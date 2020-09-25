class Foo {

    private static final String tableName = "settle_alipay_bind";

    private static final String columns =
            "momoid, real_name, account, status, create_time, update_time";

    private static final String allColumns = "id," + columns;

    private static final String insert =
            "insert into " + tableName + " ( " + columns + " ) values(?,?,?,?,?,?)";

    private static final String select = "select " + columns + " from " + tableName
            + " where momoid = ? and account = ? and real_name = ?";

    private static final String selectByMomoid =
            "select " + columns + " from " + tableName + " where momoid = ?";

    private static final String selectAccountByMomoid =
            "select account from " + tableName + " where momoid = ?";

    private static final String deleteByMomoid = "delete from " + tableName + " where momoid = ?";


}