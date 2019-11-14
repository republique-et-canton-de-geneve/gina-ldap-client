/*
 * GINA LDAP client
 *
 * Copyright 2016-2019 Republique et canton de Genève
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gina.impl.util;

import gina.impl.GinaException;

import java.util.ArrayList;
import java.util.List;

public class DnPart {

    private final String attr;

    private final String value;

    public DnPart(String attr, String value) {
        this.attr = attr;
        this.value = value;
    }

    public static DnPart[] parse(String dn) {
        List<DnPart> result = new ArrayList<>();
        char chars[] = dn.toCharArray();
        int attrStart = 0;
        int attrLen = 0;
        StringBuilder value = new StringBuilder();
        int state = 0;
        int i = 0;
        int valueLength = 0;
        while (i < chars.length) {
            char c = chars[i++];
            switch (state) {
            case 0:
                if (Character.isWhitespace(c)) {
                    // stay at state 0
                } else if (Character.isLetter(c)) {
                    attrStart = i-1;
                    state = 1;
                } else {
                    throw new GinaException("Invalid DN: name expected [" + dn + "] at position " + (i - 1));
                }
                break;
            case 1:
                if (Character.isLetter(c) || Character.isDigit(c) || c == '-') {
                    // stay at state 1
                } else {
                    --i;
                    attrLen = i-attrStart;
                    state = 2;
                }
                break;
            case 2:
                if (Character.isWhitespace(c)) {
                    // stay at state 2
                } else if (c == '=') {
                    value.setLength(0);
                    valueLength = 0;
                    state = 3;
                } else {
                    throw new GinaException("Invalid DN: '=' expected [" + dn + "] at position " + (i-1));
                }
                break;
            case 3:
                if (Character.isWhitespace(c)) {
                    // stay at state 3
                } else {
                    --i;
                    state = 4;
                }
                break;
            case 4:
                if (c == '\\') {
                    state = 5;
                } else if (c == ',') {
                    String attr = new String(chars, attrStart, attrLen);
                    String val = value.substring(0, valueLength);
                    result.add(new DnPart(attr, val));
                    state = 0;
                } else if (Character.isWhitespace(c)) {
                    value.append(c);
                } else {
                    value.append(c);
                    valueLength = value.length();
                }
                break;
            case 5:
                value.append(c);
                valueLength = value.length();
                state = 4;
                break;
            }
        }
        if (state == 3 || state == 4) {
            String attr = new String(chars, attrStart, attrLen);
            String val = value.substring(0, valueLength);
            result.add(new DnPart(attr, val));
        } else {
            throw new GinaException("Invalid DN: [" + dn + "] at position "
                    + (i-1));
        }
        return result.toArray(new DnPart[result.size()]);
    }

    public static String toString(DnPart[] parts) {
        return toString(parts, 0, parts.length);
    }

    public static String toString(DnPart[] parts, int start) {
        return toString(parts, start, parts.length);
    }

    public static String toString(DnPart[] parts, int start, int end) {
        StringBuilder buf = new StringBuilder();
        for (int i = start; i < end; ++i) {
            if (i > start) {
                buf.append(',');
            }
            buf.append(parts[i].attr);
            buf.append('=');
            escape(buf, parts[i].value);
        }
        return buf.toString();
    }

    public static String toString(String attr, String value) {
        StringBuilder buf = new StringBuilder();
        buf.append(attr);
        buf.append('=');
        escape(buf, value);
        return buf.toString();
    }

    public String getAttr() {
        return attr;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DnPart)) {
            return false;
        }
        DnPart other = (DnPart) obj;
        return attr.equals(other.attr) && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        int h = attr.hashCode();
        h = 37*h + value.hashCode();
        return h & 0x7FFFFFFF;
    }

    @Override
    public String toString() {
        return toString(attr, value);
    }

    public boolean equalsIgnoreCase(DnPart other) {
        return other != null
                && attr.equalsIgnoreCase(other.attr)
                && value.equalsIgnoreCase(other.value);
    }

    private static void escape(StringBuilder buf, String value) {
        boolean wasSpace = false;
        int start = buf.length();
        for (char c: value.toCharArray()) {
            if (wasSpace) {
                buf.append(' ');
                wasSpace = false;
            }
            switch (c) {
                case ' ':
                    if (buf.length() == start) {
                        buf.append('\\');
                    } else {
                        wasSpace = true;
                        continue;
                    }
                    break;
                case '#':
                    if (buf.length() == start) {
                        buf.append('\\');
                    }
                    break;
                case ',':
                case '+':
                case '"':
                case '\\':
                case '<':
                case '>':
                case ';':
                    buf.append('\\');
            }
            buf.append(c);
        }
    }

}
