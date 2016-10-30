package com.jiou.support;

import org.apache.commons.lang3.StringUtils;

public class UnicodeUtils {
	public static String decode(String str) {
		if (StringUtils.isBlank(str)) {
			return StringUtils.EMPTY;
		}

		char aChar;

		int len = str.length();

		StringBuilder outBuffer = new StringBuilder(len);

		for (int x = 0; x < len;) {

			aChar = str.charAt(x++);

			if (aChar == '\\') {

				aChar = str.charAt(x++);

				if (aChar == 'u') {

					int value = 0;

					for (int i = 0; i < 4; i++) {

						aChar = str.charAt(x++);

						switch (aChar) {

						case '0':

						case '1':

						case '2':

						case '3':

						case '4':

						case '5':

						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed   \\uxxxx   encoding.");
						}

					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';

					else if (aChar == 'n')

						aChar = '\n';

					else if (aChar == 'f')

						aChar = '\f';

					outBuffer.append(aChar);

				}

			} else
				outBuffer.append(aChar);
		}

		return outBuffer.toString();
	}

}
