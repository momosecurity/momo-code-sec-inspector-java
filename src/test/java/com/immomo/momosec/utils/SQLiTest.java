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
        Assert.assertFalse(check("select " /* ${field} */));
        Assert.assertFalse(check("select id," /* ${field} */));
        Assert.assertFalse(check("select id,name from " /* ${table} */));
        Assert.assertFalse(check("select id,name from table inner join" /* ${table} */));
//        Assert.assertFalse(check("select id,name from table where " /* ${field} = 1 */));
        Assert.assertTrue( check("select id,name from table where name = " /* ${value} */));
//        Assert.assertFalse(check("select id,name from table where id = 1 and " /* ${field} = 3*/));
        Assert.assertTrue( check("select id,name from table where id = 1 and name= " /* ${value} */));
        Assert.assertFalse(check("select id,name from table order by " /* ${field} */));
        Assert.assertFalse(check("select id,name from table order by id, " /* ${field} */));
        Assert.assertFalse(check("select name,sum(score) from table group by " /* ${field} */));
        Assert.assertFalse(check("select name,sum(score) from table group by sum(" /* ${field}) */));
        Assert.assertFalse(check("select name,sum(score) from table group by sum(score)," /* ${field} */));
        Assert.assertFalse(check("select name,sum(score) from table group by sum(score) having " /* ${field} */));
        Assert.assertFalse(check("select name,sum(score) from table group by sum(score) having sum(" /* ${field}) > 0 */));
        Assert.assertFalse(check("insert into " /* ${table} */));
        Assert.assertFalse(check("insert into TABLE(" /* ${field} */));
        Assert.assertFalse(check("insert into TABLE(id," /* ${field} */));
        Assert.assertTrue( check("insert into TABLE(id,name) values(" /* ${value} */));
        Assert.assertFalse(check("update " /* ${table} */));
        Assert.assertFalse(check("update TABLE set " /* ${field}=1 */));
        Assert.assertTrue( check("update TABLE set id=" /* ${value} */));
//        Assert.assertFalse(check("update TABLE set id=1, " /* ${field} */));
        Assert.assertTrue( check("update TABLE set id=1, name=" /* ${value} */));
        Assert.assertFalse(check("delete from " /* ${table} */));
//        Assert.assertFalse(check("delete from TABLE where " /* ${field}=1 */));
        Assert.assertTrue( check("delete from TABLE where name=" /* ${value} */));

    }

    private boolean check(String fragment){
        return SQLi.hasVulOnAdditiveFragments(Collections.singletonList(fragment));
    }
}
