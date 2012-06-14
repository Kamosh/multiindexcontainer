/*
 *  Main authors:
 *     Fekete Kamosh <fekete.kamosh@gmail.com> 
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class Parallel<T> {
	private static final int CORES = Runtime.getRuntime().availableProcessors();
	private static ExecutorService executor = Executors
			.newFixedThreadPool(CORES);

	static <T> void For(Iterable<T> params,
			final Operation<T> operation) {
		// Wrap operation into ResultOperation to have only one implementation 
		final ResultOperation<T, Void> resultOperation = new ResultOperation<T, Void>() {
		  public Void perform(T param) {
			  operation.perform(param);
			  return null;
		  };
		};
		For(params, resultOperation);
	}
	
	static <T, R> Iterable<R> For(Iterable<T> params,
			final ResultOperation<T, R> operation) {
		final Collection<Future<R>> futureResults = new ArrayList<Future<R>>();
		final Collection<R> results = new ArrayList<R>(futureResults.size());
		for (final T param : params) {
			Future<R> future = executor.submit(new Callable<R>() {
				@Override
				public R call() {
					return operation.perform(param);
				};
			});
			futureResults.add(future);

		}
		// Wait for all operations till they end and gather results 
		for (Future<R> f : futureResults) {
			try {
				results.add(f.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return results;
	}

	public interface Operation<T> {
		public abstract void perform(T param);
	}
	
	public interface ResultOperation<T, R> {
		public abstract R perform(T param);
	}

}
