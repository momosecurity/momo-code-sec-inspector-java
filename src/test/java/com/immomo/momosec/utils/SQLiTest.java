/*
 * Copyright 2020 momosecurity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.immomo.momosec.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class SQLiTest {

    @Test
    public void main() {
        Assert.assertFalse(check("select " /* ${field} */, "field", null));
        Assert.assertFalse(check("select id," /* ${field} */, "field", null));
        Assert.assertFalse(check("select id,name from " /* ${table} */, "table", null));
        Assert.assertFalse(check("select id,name from table inner join" /* ${table} */, "table", null));
        Assert.assertFalse(check("select id,name from table where ", "field", " = 1"));
        Assert.assertTrue( check("select id,name from table where name = " /* ${value} */, "value", null));
        Assert.assertFalse(check("select id,name from table where id = 1 and " /* ${field} = 3*/, "field", " = 3"));
        Assert.assertTrue( check("select id,name from table where id = 1 and name= " /* ${value} */, "value", null));
        Assert.assertFalse(check("select id,name from table order by " /* ${field} */, "field", null));
        Assert.assertFalse(check("select id,name from table order by id, " /* ${field} */, "field", null));
        Assert.assertFalse(check("select name,sum(score) from table group by " /* ${field} */, "field", null));
        Assert.assertFalse(check("select name,sum(score) from table group by sum(" /* ${field}) */, "field", ")"));
        Assert.assertFalse(check("select name,sum(score) from table group by sum(score)," /* ${field} */, "field", null));
        Assert.assertFalse(check("select name,sum(score) from table group by sum(score) having " /* ${field} */, "field", null));
        Assert.assertFalse(check("select name,sum(score) from table group by sum(score) having sum(" /* ${field}) > 0 */, "field", ") > 0"));
        Assert.assertFalse(check("insert into " /* ${table} */, "table", null));
        Assert.assertFalse(check("insert into TABLE(" /* ${field}) */, "fields", ")"));
        Assert.assertFalse(check("insert into TABLE(id," /* ${field} */, "field", ")"));
        Assert.assertTrue( check("insert into TABLE(id,name) values(" /* ${value} */, "value", ")"));
        Assert.assertFalse(check("update " /* ${table} */, "table", null));
        Assert.assertFalse(check("update TABLE set " /* ${field}=1 */, "field", "=1"));
        Assert.assertTrue( check("update TABLE set id=" /* ${value} */, "value", null));
        Assert.assertFalse(check("update TABLE set id=1, " /* ${field}=2 */, "field", "=2"));
        Assert.assertTrue( check("update TABLE set id=1, name=" /* ${value} */, "value", null));
        Assert.assertFalse(check("delete from " /* ${table} */, "field", null));
        Assert.assertFalse(check("delete from TABLE where " /* ${field}=1 */, "field", "=1"));
        Assert.assertTrue( check("delete from TABLE where name=" /* ${value} */, "value", null));

    }

    private boolean check(String prefix, String var, String suffix){
        return SQLi.hasVulOnSQLJoinStr(prefix, var, suffix);
    }
}
