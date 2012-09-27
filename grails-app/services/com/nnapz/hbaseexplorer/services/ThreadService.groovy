package com.nnapz.hbaseexplorer.services
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Some tasks are to be done asynchronous by this server. The Quartz framework seemed to be to tailored for repeated
 * execution, also, a job instance naming logic is missing.
 *
 * You typically hand over a closure to execute() and then ask later for the status of your job. Naming is on your
 * duty.
 *
 * setStatus()/getStatus() offers a small handover facility, e.g. to display a status information.
 * 
 * @author Bob Schulze
 */
class ThreadService {

    boolean transactional = true
    // static scope = "singleton"  (is default)

    // keeps the executing threads (until cleaned by isRunning()
    HashMap<String, Thread> executingThreads = (HashMap<String, Thread>) Collections.synchronizedMap(new HashMap<String, Thread>())
    // keeps some status information (optional set by setStatus()
    HashMap<String, String> threadStatus = (HashMap<String, String>) Collections.synchronizedMap(new HashMap<String, String>())

    /**
     * Execute a new Thread with a given name.
     * @return false, if the thread was not executed, because a running thread with the given name exists already.
     */
    synchronized boolean execute(String threadName, Closure runnable ) {
      if (isRunning(threadName)) return false
      Thread thread = Thread.start(threadName, runnable)
      executingThreads.put(threadName, thread)
      return true
    }

   /**
    * Check if a thread is running.
    * @return true, if the named thread is running.
    */
    synchronized boolean isRunning(String threadName) {
      Thread theOne = executingThreads.get(threadName)
      if (theOne == null) return false
      if (theOne.isAlive()) return true;
      // its dead
      return false
    }


   /**
    * Cleanup thread list 
    */
    public void cleanup() {
      def deadList = []
      executingThreads.each{String k, Thread v ->
        if (!isRunning(k))  deadList << k
      }
      deadList.each{ String threadName ->
        executingThreads.remove(threadName)
        threadStatus.remove(threadName)
      }
    }
   /**
    * Provide a list of threads currently running.
    * Cleans up old threads from the list, should there be any.
    */
    public def getThreads() {
      cleanup();
      def ret = []
      executingThreads.each{String k, Thread v ->
        if (isRunning(k))  ret << v
      }
      return ret;
    }

   /**
    * Blocks until the closure returns true. Called from within the execute()-Closures, within the same thread, thats why
    * there is no thread reference here.
    * @see HbaseService#pushTableStats on how to use it.
    * @tester a closure that should return true if we should stop waiting
    */
    public boolean waitFor(int tests, int milliSeconds, Closure tester) {
      while (!tester.call()) {
        Thread.sleep(milliSeconds);
        tests --;
        if (tests <= 0) return false
      }
      return true
    }

   /**
    * Offer a way to store status information to a thread.
    */
    public void setStatus(String threadName, String newStatusMessage) {
      threadStatus.put(threadName, newStatusMessage);
    }

   /**
    * Get the status information for the given thread.
    */
    public String getStatus(String threadName) {
      return threadStatus.get(threadName)
    }
}
