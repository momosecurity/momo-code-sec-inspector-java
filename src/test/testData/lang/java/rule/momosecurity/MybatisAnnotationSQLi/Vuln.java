import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface Vuln {

    @Select(<error descr="MomoSec: Mybatis注解SQL注入漏洞">"select age from user where id = ${id}"</error>)
    List<Integer> selectUserAge_vuln(@Param("id") Integer id);

    @Select("select age from user where id = #{id}")
    List<Integer> selectUserAge(@Param("id") Integer id);

    @Select("select age from ${table} where id = #{id}")
    List<Integer> selectTableIgnore(@Param("table") String table, @Param("id") Integer id);

    @Select(<error descr="MomoSec: Mybatis注解SQL注入漏洞">{
        "<script>" +
            "select age from user where id in (${ids})" +
        "</script>"
    }</error>)
    List<Integer> selectUserAgs_vuln(@Param("ids") String ids);

    @Select({
        "<script>" +
            "select age from user where id in" +
                "<foreach item='id' collection='ids' open='(' separator=',' close=')' >" +
                "   #{id}" +
                "</foreach>" +
        "</script>"
    })
    List<Integer> selectUserAgs(@Param("ids") List<Integer> ids);

    @Select({
        "<script>",
            "select age from user where id in",
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>",
                "#{id}",
            "</foreach>",
        "</script>"
    })
    List<Integer> selectArrayExample(@Param("ids") List<Integer> ids);

    @Select(<error descr="MomoSec: Mybatis注解SQL注入漏洞">"select count(1) from user " + "where id in ${ids}"</error>)
    int countUser(@Param("ids") String ids);
}