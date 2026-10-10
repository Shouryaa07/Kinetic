package com.kinetic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.kinetic.exception.ValidationException;
import com.kinetic.util.Html;
import com.kinetic.util.JsonLite;
import com.kinetic.util.Validate;
import java.util.Map;
import org.junit.Test;

public class ValidateAndJsonTest {
    @Test
    public void emailIsNormalisedAndChecked() throws Exception {
        assertEquals("a@b.co", Validate.email("  A@B.co "));
        assertThrows(ValidationException.class, () -> Validate.email("nope"));
    }

    @Test
    public void passwordNeedsLetterAndDigit() {
        assertThrows(ValidationException.class, () -> Validate.password("short1"));
        assertThrows(ValidationException.class, () -> Validate.password("onlyletters"));
        assertThrows(ValidationException.class, () -> Validate.password("12345678"));
    }

    @Test
    public void numbersAreRangeChecked() throws Exception {
        assertEquals(30, Validate.integer("30", "Duration", 1, 100));
        assertThrows(ValidationException.class, () -> Validate.integer("0", "Duration", 1, 100));
        assertThrows(ValidationException.class, () -> Validate.integer("abc", "Duration", 1, 100));
        assertEquals(5.5, Validate.decimal("5,5", "Distance", 0, 10), 0.0001);
        assertEquals(0, Validate.decimal("", "Distance", 0, 10), 0.0001);
    }

    @Test
    public void htmlIsEscaped() {
        assertEquals("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;", Html.esc("<script>alert('x')</script>"));
    }

    @Test
    public void jsonLiteParsesFlatObjects() {
        Map<String, String> m = JsonLite.parseObject("{\"aud\":\"abc\",\"exp\":\"1700\",\"name\":\"Jos\\u00e9 \\\"J\\\"\",\"email_verified\":true}");
        assertEquals("abc", m.get("aud"));
        assertEquals("1700", m.get("exp"));
        assertEquals("true", m.get("email_verified"));
        assertEquals("José \"J\"", m.get("name"));
    }
}
