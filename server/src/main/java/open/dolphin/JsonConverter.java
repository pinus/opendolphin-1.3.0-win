package open.dolphin;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import open.dolphin.infomodel.IInfoModel;

import javax.ejb.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Jackson ObjectMapper
 *
 * @author pns
 */
@Provider
@Singleton
public class JsonConverter implements ContextResolver<ObjectMapper> {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Hibernate5Module hbm = new Hibernate5Module();

    static {
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        PolymorphicTypeValidator ptv = new PolymorphicTypeValidator() {

            @Override
            public Validity validateBaseType(MapperConfig<?> config, JavaType baseType) {
                System.out.println("-------------- base " + baseType);
                return Validity.INDETERMINATE;
            }

            @Override
            public Validity validateSubClassName(MapperConfig<?> config, JavaType baseType, String subClassName) throws JsonMappingException {
                System.out.println("-------------- base " + baseType);
                System.out.println("----------subclass " + subClassName);
                return Validity.INDETERMINATE;
            }

            @Override
            public Validity validateSubType(MapperConfig<?> config, JavaType baseType, JavaType subType) throws JsonMappingException {
                System.out.println("-------------- base " + baseType);
                System.out.println("----------subtype " + subType);
                return Validity.ALLOWED;
            }
        };
        PolymorphicTypeValidator ptv2 = BasicPolymorphicTypeValidator.builder()
            .build();
        //mapper.activateDefaultTyping(ptv2);

        hbm.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, false);
        hbm.configure(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION, false);
        mapper.registerModule(hbm);
    }

    public JsonConverter() {
        System.out.println("JsonConverter: ObjectMapper configured.");
    }

    /**
     * Utility method to test converter
     *
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return null;
    }

    /**
     * Utility method to test converter
     *
     * @param <T>
     * @param json
     * @param clazz
     * @return
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return null;
    }

    /**
     * Provides ObjectMapper for resteasy
     *
     * @param type
     * @return
     */
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
