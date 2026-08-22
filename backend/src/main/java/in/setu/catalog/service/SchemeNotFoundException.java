package in.setu.catalog.service;

public class SchemeNotFoundException extends RuntimeException {
    public SchemeNotFoundException(String code) { super("No published synthetic demo scheme found for code: " + code); }
}
