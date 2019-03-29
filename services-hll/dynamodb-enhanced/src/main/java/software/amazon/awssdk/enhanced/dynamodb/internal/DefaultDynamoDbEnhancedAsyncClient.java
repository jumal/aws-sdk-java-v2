package software.amazon.awssdk.enhanced.dynamodb.internal;

import java.util.Collection;
import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.enhanced.dynamodb.AsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.converter.ItemAttributeValueConverter;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.ItemAttributeValueConverterChain;
import software.amazon.awssdk.enhanced.dynamodb.internal.model.DefaultAsyncTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@SdkInternalApi
@ThreadSafe
public class DefaultDynamoDbEnhancedAsyncClient implements DynamoDbEnhancedAsyncClient {
    private boolean shouldCloseUnderlyingClient;
    private final DynamoDbAsyncClient client;
    private final ItemAttributeValueConverterChain converter;

    private DefaultDynamoDbEnhancedAsyncClient(Builder builder) {
        if (builder.client == null) {
            this.client = DynamoDbAsyncClient.create();
            this.shouldCloseUnderlyingClient = true;
        } else {
            this.client = builder.client;
            this.shouldCloseUnderlyingClient = false;
        }

        this.converter = builder.converterChain.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public AsyncTable table(String tableName) {
        return DefaultAsyncTable.builder()
                                .converter(converter)
                                .dynamoDbAsyncClient(client)
                                .name(tableName)
                                .build();
    }

    @Override
    public void close() {
        if (shouldCloseUnderlyingClient) {
            client.close();
        }
    }

    @Override
    public Builder toBuilder() {
        throw new UnsupportedOperationException();
    }

    public static class Builder implements DynamoDbEnhancedAsyncClient.Builder {
        private ItemAttributeValueConverterChain.Builder converterChain =
                ItemAttributeValueConverterChain.builder()
                                                .parent(DefaultConverterChain.create());
        private DynamoDbAsyncClient client;

        private Builder() {}

        @Override
        public Builder dynamoDbClient(DynamoDbAsyncClient client) {
            this.client = client;
            return this;
        }

        @Override
        public Builder addConverters(Collection<? extends ItemAttributeValueConverter> converters) {
            converterChain.addConverters(converters);
            return this;
        }

        @Override
        public Builder addConverter(ItemAttributeValueConverter converter) {
            converterChain.addConverter(converter);
            return this;
        }

        @Override
        public Builder clearConverters() {
            converterChain.clearConverters();
            return this;
        }

        @Override
        public DynamoDbEnhancedAsyncClient build() {
            return new DefaultDynamoDbEnhancedAsyncClient(this);
        }
    }
}