package xyz.lokili.loutils.utils.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Результат валидации конфига
 */
public class ValidationResult {
    
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    private final List<ConfigFix> fixes;
    
    private ValidationResult(boolean valid, List<String> errors, List<String> warnings, List<ConfigFix> fixes) {
        this.valid = valid;
        this.errors = errors;
        this.warnings = warnings;
        this.fixes = fixes;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public List<ConfigFix> getFixes() {
        return fixes;
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public boolean hasFixes() {
        return !fixes.isEmpty();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static ValidationResult success() {
        return new Builder().valid(true).build();
    }
    
    public static ValidationResult error(String error) {
        return new Builder().valid(false).error(error).build();
    }
    
    public static class Builder {
        private boolean valid = true;
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final List<ConfigFix> fixes = new ArrayList<>();
        
        public Builder valid(boolean valid) {
            this.valid = valid;
            return this;
        }
        
        public Builder error(String error) {
            this.errors.add(error);
            this.valid = false;
            return this;
        }
        
        public Builder warning(String warning) {
            this.warnings.add(warning);
            return this;
        }
        
        public Builder fix(ConfigFix fix) {
            this.fixes.add(fix);
            return this;
        }
        
        public Builder fix(String path, Object value, String reason) {
            return fix(new ConfigFix(path, value, reason));
        }
        
        public ValidationResult build() {
            return new ValidationResult(valid, errors, warnings, fixes);
        }
    }
    
    /**
     * Автоматическое исправление конфига
     */
    public static class ConfigFix {
        private final String path;
        private final Object value;
        private final String reason;
        
        public ConfigFix(String path, Object value, String reason) {
            this.path = path;
            this.value = value;
            this.reason = reason;
        }
        
        public String getPath() {
            return path;
        }
        
        public Object getValue() {
            return value;
        }
        
        public String getReason() {
            return reason;
        }
    }
}
