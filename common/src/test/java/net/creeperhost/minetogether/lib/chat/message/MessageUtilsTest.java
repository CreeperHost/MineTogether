package net.creeperhost.minetogether.lib.chat.message;

import org.junit.jupiter.api.Test;

import static net.creeperhost.minetogether.lib.test.MockUtils.MOCK_PROFILE_MANAGER;
import static net.creeperhost.minetogether.lib.test.MockUtils.TEST_USER_HASH;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by covers1624 on 4/11/22.
 */
public class MessageUtilsTest {

    @Test
    public void testParseMessage() {
        assertEquals("Before User#7A70B", MessageUtils.parseMessage(MOCK_PROFILE_MANAGER, "Before " + TEST_USER_HASH).toString());
        assertEquals("Before User#7A70B After", MessageUtils.parseMessage(MOCK_PROFILE_MANAGER, "Before " + TEST_USER_HASH + " After").toString());
        assertEquals("Before User#7A70B Middle User#7A70B After", MessageUtils.parseMessage(MOCK_PROFILE_MANAGER, "Before " + TEST_USER_HASH + " Middle " + TEST_USER_HASH + " After").toString());
    }

    @Test
    public void testOutgoingMessage() {
        assertEquals("Hello, World!", MessageUtils.processOutboundMessage(MOCK_PROFILE_MANAGER, "§cHello§r, §bWorld§r!"));
    }
}
