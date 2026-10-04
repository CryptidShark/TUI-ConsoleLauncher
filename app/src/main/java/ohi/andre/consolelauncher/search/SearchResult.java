package ohi.andre.consolelauncher.search;

public class SearchResult implements Comparable<SearchResult> {

    public enum Category {
        APPLICATION(100),
        ACTION(90),
        COMMAND(80),
        ALIAS(70),
        SETTINGS(60),
        SYSTEM(50);

        private final int baseWeight;

        Category(int baseWeight) {
            this.baseWeight = baseWeight;
        }

        public int getBaseWeight() {
            return baseWeight;
        }
    }

    private final String title;
    private final String subtitle;
    private final Category category;
    private final int score;
    private final SearchAction action;

    public SearchResult(String title, String subtitle, Category category, int score, SearchAction action) {
        this.title = title;
        this.subtitle = subtitle;
        this.category = category;
        this.score = score;
        this.action = action;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Category getCategory() {
        return category;
    }

    public int getScore() {
        return score;
    }

    public SearchAction getAction() {
        return action;
    }

    public int getFinalWeight() {
        return score + (category != null ? category.getBaseWeight() : 0);
    }

    @Override
    public int compareTo(SearchResult other) {
        if (other == null) return -1;
        return Integer.compare(other.getFinalWeight(), this.getFinalWeight());
    }
}
