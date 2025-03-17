package looking_glass.query;

import looking_glass.common.Utils;

// Represents a query that we can run on the database. It has a title and a text.
// The title is displayed in the sidebar in the UI and the text is shown in the
// text area. The text is the actual query that will be run on the database.
public class Query {
    public String title;
    public String text;

    // Constructor.
    public Query(String title, String text) {
        this.title = title;
        this.text = text;
    }

    // Convert the query to a JSON string.
    public String toJson() throws Exception {
        return Utils.toJson(this);
    }

    // Overriding the toString method to return the title of the query, this
    // will make the query title to be displayed in the sidebar.
    @Override
    public String toString() {
        return title;
    }
}
