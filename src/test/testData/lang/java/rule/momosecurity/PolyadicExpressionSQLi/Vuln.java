import org.apache.commons.lang3.StringUtils;
import java.util.Set;

public class Vuln {

    private final String SQL_TEMPLATE = "select * from template where id = ";

    private static class LOG {
        public static void debug(String msg) {}
    }

    void log(String msg) {}

    String getTable() {
        return "";
    }

    enum STATUS {
        ;
        public static String getUnique() {
            return "0";
        }
    }

    void bar1(String id, String table, String ids) {
        String sql1 = "select * from table1 where id = 1";

        String sql2 = <error descr="MomoSec: 疑似多项式拼接SQL注入漏洞">"select * from table2 where id = " + id + " and name != " + id</error>;

        String sql3 = <error descr="MomoSec: 疑似多项式拼接SQL注入漏洞">SQL_TEMPLATE + id</error>;

        String sql4 = "select *  from " + table + " where id = 1";
        String sql5 = "select *  from " + getTable() + " where id = 1";

        String sql6 = <error descr="MomoSec: 疑似多项式拼接SQL注入漏洞">"select *  from " + getTable() + " where id in (" + ids +")"</error>;

        String sql7 = <error descr="MomoSec: 疑似占位符拼接SQL注入漏洞">"select *  from " + getTable() + " where id in (%s)"</error>;

        Integer limit = 1;
        String sql8 = "select * from table limit " + limit;

        Set<Integer> numbers = null;
        String sql9 = "select * from table where id in (" + StringUtils.join(numbers, ",") + ")";

        String sql10 = "select * from table where id = " + STATUS.getUnique();

        LOG.debug("select * from table_log where id = " + id);
        this.log("select * from table_log where id = " + id);

    }
}