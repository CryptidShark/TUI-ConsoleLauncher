package ohi.andre.consolelauncher.managers;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class AutomationEngine {

    public static class Rule {
        public final String condition;
        public final String action;
        public final boolean enabled;

        public Rule(String condition, String action, boolean enabled) {
            this.condition = condition;
            this.action = action;
            this.enabled = enabled;
        }
    }

    private final List<Rule> rules = new ArrayList<>();

    public AutomationEngine() {
        // Default sample WHEN-DO rules
        rules.add(new Rule("WHEN battery < 20%", "DO open battery saver settings", true));
        rules.add(new Rule("WHEN time = 08:00", "DO workspace = Developer", true));
    }

    public List<Rule> getRules() {
        return new ArrayList<>(rules);
    }

    public void addRule(String condition, String action) {
        if (condition != null && action != null) {
            rules.add(new Rule(condition, action, true));
        }
    }
}
