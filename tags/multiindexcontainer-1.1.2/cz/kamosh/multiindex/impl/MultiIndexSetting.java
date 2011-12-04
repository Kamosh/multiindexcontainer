/*
 *  Main authors:
 *     Fekete Kamosh <fekete.kamosh@gmail.com> 
 * 
 *     
 *  Last modified:
 *     $Date$ by $Author$
 *     $Revision$
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package cz.kamosh.multiindex.impl;


/**
 * Singleton class which holds settings for multiindex behavior.
 * To enable/disable multicore behavior, use {@link #setUseParalellization(boolean)}.
 * 
 * @author fekete
 *
 */
public final class MultiIndexSetting {

	private MultiIndexSetting() {};
	
	static MultiIndexSetting instance = new MultiIndexSetting();
	
	public static MultiIndexSetting getInstance() {
		return instance;
	}
	
	private boolean useParalellization = true;

	/**
	 * @return Should be used multithreading on multicore computers?
	 */
	public boolean isUseParalellization() {
		return useParalellization;
	}

	/**
	 * Should be used multithreading on multicore computers?
	 * @param useParalellization  Use multithreading?
	 */
	public void setUseParalellization(boolean useParalellization) {
		this.useParalellization = useParalellization;
	}
		
}
