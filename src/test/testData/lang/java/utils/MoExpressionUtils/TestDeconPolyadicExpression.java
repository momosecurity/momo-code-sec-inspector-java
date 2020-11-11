public class TestDerefExpression {
    private String baseFields = "username, password";
    private String extFields = baseFields + ", host";
    enum UserType {
        OPEN,
        OFFICE
    }

    public void polyadicWithField() {
        String sql = "select " + extFields + " from User " + " where type = " + UserType.OFFICE;
    }

    public void polyadicWithArgs(String[] args) {
        String id = args[0];
        String sql = "select * from T where id = " + id;
    }

    public void polyadicWithLiteral() {
        String id = "1";
        String sql = "select * from T where id = " + id;
    }

    public void polyadicWithMultiLayerLiteral() {
        String where = "(";
        for(int i=0; i<3; i++) {
            where +=  "1,";
            where +=  "2" + ",";
        }
        where = where + ")";
        String sql = "select * from T " + where;
    }

    public void polyadicWithMultiLayerVar(String id) {
        String where = "(";
        for(int i=0; i<3; i++) {
            where +=  id + ",";
        }
        where = where + ")";
        String sql = "select * from T " + where;
    }

    public void polyadicWithStringBuilder(String ids) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(ids);
        sb.append(")");
        String sql = "select * from T where id in " + sb;
    }

    public void ignore() {
        String where = "1";
        where = where;
        String sql = "select * from T " + where;
    }
}