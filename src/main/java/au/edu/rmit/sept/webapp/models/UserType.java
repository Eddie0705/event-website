package au.edu.rmit.sept.webapp.models;

/**
 * Enum representing the different types of users in the system
 * Maps to the user_type ENUM in the database
 */
public enum UserType {
    STUDENT("student"),
    ORGANISER("organiser"),
    ADMIN("admin");

    private final String value;

    UserType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Convert string value to UserType enum
     * 
     * @param value the string value
     * @return the corresponding UserType
     * @throws IllegalArgumentException if value is not valid
     */
    public static UserType fromValue(String value) {
        for (UserType type : UserType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid user type: " + value);
    }
}
