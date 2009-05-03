package com.quui.notes.editor.data;

/**
 * Filter notes using different modes.
 * @author Fabian Steeg (fsteeg)
 */
public final class NotesFilter {
    private NotesFilter() {/* Util class */}
    /**
     * @param note The note
     * @param query The query
     * @param mode The search mode
     * @return True, if the query matches the note, using the given mode
     */
    public static boolean matches(Note note, String query, SearchOption mode) {
        return mode.matches(note, query);
    }

    /** Options for filtering the notes list. */
    public static enum SearchOption {
        TITLE {
            /**
             * {@inheritDoc}
             * @see com.quui.notes.editor.data.NotesFilter.SearchOption#matches(com.quui.notes.editor.data.Note,
             *      java.lang.String)
             */
            @Override
            boolean matches(Note note, String query) {
                return note.getTitle().toLowerCase().contains(
                        query.toLowerCase());
            }
        },
        TAG {
            /**
             * {@inheritDoc}
             * @see com.quui.notes.editor.data.NotesFilter.SearchOption#matches(com.quui.notes.editor.data.Note,
             *      java.lang.String)
             */
            @Override
            boolean matches(Note note, String query) {
                return note.getTag().toLowerCase()
                        .contains(query.toLowerCase());
            }
        },
        CONTENT {
            /**
             * {@inheritDoc}
             * @see com.quui.notes.editor.data.NotesFilter.SearchOption#matches(com.quui.notes.editor.data.Note,
             *      java.lang.String)
             */
            @Override
            boolean matches(Note note, String query) {
                return note.getText().toLowerCase().contains(
                        query.toLowerCase());
            }
        },
        ANY {
            /**
             * {@inheritDoc}
             * @see com.quui.notes.editor.data.NotesFilter.SearchOption#matches(com.quui.notes.editor.data.Note,
             *      java.lang.String)
             */
            @Override
            boolean matches(Note note, String query) {
                for (SearchOption op : SearchOption.values()) {
                    if (op.matches(note, query)) {
                        return true;
                    }
                }
                return false;
            }
        },
        DATE {
            /**
             * {@inheritDoc}
             * @see com.quui.notes.editor.data.NotesFilter.SearchOption#matches(com.quui.notes.editor.data.Note,
             *      java.lang.String)
             */
            @Override
            boolean matches(Note note, String query) {
                return note.getDate().toLowerCase().contains(
                        query.toLowerCase());
            }
        },
        PUBLIC {
            /**
             * {@inheritDoc}
             * @see com.quui.notes.editor.data.NotesFilter.SearchOption#matches(com.quui.notes.editor.data.Note,
             *      java.lang.String)
             */
            @Override
            boolean matches(Note note, String query) {
                return query.toLowerCase().contains("true")
                        ? note.isPublic() : !note.isPublic();
            }
        };
        /**
         * @param note The note
         * @param query The query
         * @return True, if the query matches the note
         */
        abstract boolean matches(Note note, String query);
    }
}
