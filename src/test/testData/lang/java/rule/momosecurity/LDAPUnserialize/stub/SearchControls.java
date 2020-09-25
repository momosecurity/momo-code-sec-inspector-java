package javax.naming.directory;

public class SearchControls {
    public SearchControls() {}

    public SearchControls(int scope,
                          long countlim,
                          int timelim,
                          String[] attrs,
                          boolean retobj,
                          boolean deref) {
    }

    public void setReturningObjFlag(boolean on) {
    }
}