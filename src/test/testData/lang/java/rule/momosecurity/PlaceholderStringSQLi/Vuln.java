public class Vuln {

    private void error(String e) {}

    void bar1(String id, String table, String fields) {
        String.format("select * from table where id = 1");
        String.format("select * from %s where id = 1", id);

        <error descr="MomoSec: 疑似占位符拼接SQL注入漏洞">String.format("select * from table where id = %s", id)</error>;
        <error descr="MomoSec: 疑似占位符拼接SQL注入漏洞">String.format("select * from table where id = %1$s and status = 0", id)</error>;
        String.format("select * from user where token='%s'", "selftoken");

        String lit = "lit";
        String.format("select * from table where lit='%s'", lit);

        String.format("insert into %s (%s) values(?,?)", table, fields);

        int id1 = 1;
        String.format("select * from table where id = %s", id1);

        String sql = "select * from table where id = %s and id = %s";
        String.format(sql, id1, id1);

        String sql2 = "select * from table where id1 = %s and uid = %s";
        <error descr="MomoSec: 疑似占位符拼接SQL注入漏洞">String.format(sql, id1, id)</error>;
        error(String.format(sql, id1, id));
    }
}