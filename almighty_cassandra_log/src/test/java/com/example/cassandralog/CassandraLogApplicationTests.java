package com.example.cassandralog;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.cassandralog.dto.CassandraLogPojo;
import com.example.cassandralog.dto.ClientRequestPojo;
import com.example.cassandralog.entity.CassandraLogEntity;
import com.example.cassandralog.utils.ConstantsInUse;
import com.example.cassandralog.utils.CurrentTime;

import static org.junit.jupiter.api.Assertions.*;

public class CassandraLogApplicationTests {

    @Test
    public void testClientRequestPojoMapping() {
        ClientRequestPojo pojo = new ClientRequestPojo();
        long now = 1700000000L;
        pojo.copyClientRequestData(now, "10.0.0.1", "/search", "q=test", "GET", "NONE", "ALLOW");

        assertEquals(now, pojo.getActionTime());
        assertEquals("10.0.0.1", pojo.getIp());
        assertEquals("/search", pojo.getUri());
        assertEquals("q=test", pojo.getQuery());
        assertEquals("GET", pojo.getMethod());
        assertEquals("NONE", pojo.getThreat_type());
        assertEquals("ALLOW", pojo.getAction());
    }

    @Test
    public void testCassandraLogEntityCreation() {
        UUID id = UUID.randomUUID();
        CassandraLogEntity entity = new CassandraLogEntity(
                id,
                1700000000L,
                "192.168.1.100",
                "/admin",
                "id=1%20UNION%20SELECT",
                "POST",
                "SQL_INJECTION",
                "BLOCK",
                ConstantsInUse.ACTIVE_STATUS
        );

        assertEquals(id, entity.getId());
        assertEquals("192.168.1.100", entity.getIp());
        assertEquals("SQL_INJECTION", entity.getThreat_type());
        assertEquals("BLOCK", entity.getAction());
        assertEquals(ConstantsInUse.ACTIVE_STATUS, entity.getStatus());
    }

    @Test
    public void testCassandraLogPojoGettersSetters() {
        UUID id = UUID.randomUUID();
        CassandraLogPojo pojo = new CassandraLogPojo();
        pojo.setId(id);
        pojo.setIp("127.0.0.1");
        pojo.setThreat_type("XSS");
        pojo.setAction("BLOCK");
        pojo.setStatus("active");

        assertEquals(id, pojo.getId());
        assertEquals("127.0.0.1", pojo.getIp());
        assertEquals("XSS", pojo.getThreat_type());
        assertEquals("BLOCK", pojo.getAction());
    }

    @Test
    public void testConstantsAndCurrentTime() {
        assertEquals("firewall_logs", ConstantsInUse.TOPIC);
        assertEquals("firewall_key", ConstantsInUse.KEYSPACE_NAME);
        assertEquals("success", ConstantsInUse.SUCCESS_MESSAGE);

        CurrentTime currentTime = new CurrentTime();
        assertTrue(currentTime.getCurrentTime() > 1700000000L);
    }
}
