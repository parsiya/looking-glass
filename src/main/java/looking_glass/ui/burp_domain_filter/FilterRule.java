package looking_glass.ui.burp_domain_filter;

// This class is based on the Burp `Target > Scope > Include/Exclude` section.
public class FilterRule {
    // 1. `Enabled` (checkbox)
    public boolean enabled;
    // 2. `Prefix` (string)
    public String prefix;
    // 3. `Include Subdomains` (checkbox).
    public boolean includeSubdomains;

    FilterRule() {
        this.enabled = false;
        this.prefix = "";
        this.includeSubdomains = false;
    }

    FilterRule(boolean enabled, String prefix, boolean includeSubdomains) {
        this.enabled = enabled;
        this.prefix = prefix;
        this.includeSubdomains = includeSubdomains;
    }
}
