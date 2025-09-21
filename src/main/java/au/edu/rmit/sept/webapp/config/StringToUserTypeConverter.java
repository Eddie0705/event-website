package au.edu.rmit.sept.webapp.config;

import au.edu.rmit.sept.webapp.models.UserType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToUserTypeConverter implements Converter<String, UserType> {

    @Override
    public UserType convert(String source) {
        System.out.println("Converting string: '" + source + "' to UserType");

        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        UserType result = UserType.fromValue(source.trim());
        System.out.println("Converted to: " + result);
        return result;
    }
}