/* 
 * The MIT License
 *
 * Copyright 2021 buddhika.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/**
 * Author:  buddhika
 * Created: Jun 16, 2021
 */

set foreign_key_checks=0;

ALTER TABLE reportcolumn MODIFY id INTEGER NOT NULL AUTO_INCREMENT;
ALTER TABLE reportcolumn AUTO_INCREMENT=58355;


ALTER TABLE reportrow MODIFY id INTEGER NOT NULL AUTO_INCREMENT;
ALTER TABLE reportrow AUTO_INCREMENT=58355;


ALTER TABLE reportcell MODIFY id INTEGER NOT NULL AUTO_INCREMENT;
ALTER TABLE reportcell AUTO_INCREMENT=58355;

set foreign_key_checks=1;