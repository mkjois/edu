package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.io.EOFException;

/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see nachos.vm.VMProcess
 * @see nachos.network.NetProcess
 */
public class UserProcess {
    // SKELETON CODE DECLARATIONS
    /** The number of pages in the program's stack. */
    protected final int stackPages = 8;
    /** The program being run by this process. */
    protected Coff coff;
    /** This process's page table. */
    protected TranslationEntry[] pageTable;
    /** The number of contiguous pages occupied by the program. */
    protected int numPages;
    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
    private int initialPC, initialSP;
    private int argc, argv;

    /** Maximum filename length for this file system. */
    public static final int MAX_FILENAME_SIZE = 256;

    // TASK 1 DECLARATIONS
    /** Maximum number of files a process has access to at one time. */
    private static final int FILES = 16;
    /** Table of files this process currently has access to. */
    protected OpenFile[] files = new OpenFile[FILES];
    /** Queue of invalidated file descriptors. */
    protected LinkedList<Integer> descriptors = new LinkedList<Integer>();


    // TASK 3 DECLARATIONS
    private static final Lock counterLock = new Lock();
    private static final Lock activeLock = new Lock();
    private static final Lock processesLock = new Lock();
    private static final Lock statusLock = new Lock();
    private static final int ROOT = 0;
    private static int counter = ROOT;
    private static int active = 0;
    private static final HashMap<Integer, UserProcess> processes =
        new HashMap<Integer, UserProcess>();
    private static final HashMap<Integer, Integer> statuses =
        new HashMap<Integer, Integer>();
    private HashSet<Integer> children = new HashSet<Integer>();
    private Lock childLock = new Lock();
    private int pid;
    private boolean exitedNormally;
    private UThread thread;

    public static final int WORD_LENGTH = 4;

    /**
     * Allocate a new process.
     */
    public UserProcess() {
      //Multiprogramming -> remove above code and uncomment below
//      loadSections();
      
      
      // initialize file array for file-related syscalls
      for (int i=0; i<FILES; i++) {
          this.descriptors.offer(i);
      }
      this.files[this.descriptors.poll()] = UserKernel.console.openForReading();
      this.files[this.descriptors.poll()] = UserKernel.console.openForWriting();
  }

    /**
     * Allocate and return a new process of the correct class. The class name
     * is specified by the <tt>nachos.conf</tt> key
     * <tt>Kernel.processClassName</tt>.
     *
     * @return  a new process of the correct class.
     */
    public static UserProcess newUserProcess() {
        return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
    }

    /**
     * Execute the specified program with the specified arguments. Attempts to
     * load the program, and then forks a thread to run it.
     *
     * @param   name    the name of the file containing the executable.
     * @param   args    the arguments to pass to the executable.
     * @return  <tt>true</tt> if the program was successfully executed.
     */
    public boolean execute(String name, String[] args) {
        if (!load(name, args))
            return false;

        thread = (UThread) new UThread(this).setName(name);
        thread.fork();

        return true;
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
        Machine.processor().setPageTable(pageTable);
    }

    /**
     * Read a null-terminated string from this process's virtual memory. Read
     * at most <tt>maxLength + 1</tt> bytes from the specified address, search
     * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
     * without including the null terminator. If no null terminator is found,
     * returns <tt>null</tt>.
     *
     * @param   vaddr   the starting virtual address of the null-terminated
     *                  string.
     * @param   maxLength       the maximum number of characters in the string,
     *                          not including the null terminator.
     * @return  the string read, or <tt>null</tt> if no null terminator was
     *          found.
     */
    public String readVirtualMemoryString(int vaddr, int maxLength) {
        Lib.assertTrue(maxLength >= 0);

        byte[] bytes = new byte[maxLength+1];

        int bytesRead = readVirtualMemory(vaddr, bytes);

        for (int length=0; length<bytesRead; length++) {
            if (bytes[length] == 0)
                return new String(bytes, 0, length);
        }

        return null;
    }

    /**
     * Transfer data from this process's virtual memory to all of the specified
     * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param   vaddr   the first byte of virtual memory to read.
     * @param   data    the array where the data will be stored.
     * @return  the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data) {
        return readVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from this process's virtual memory to the specified array.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param   vaddr   the first byte of virtual memory to read.
     * @param   data    the array where the data will be stored.
     * @param   offset  the first byte to write in the array.
     * @param   length  the number of bytes to transfer from virtual memory to
     *                  the array.
     * @return  the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data, int offset,
            int length) {
	//Keep this line
	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

//	System.out.println("Supposed to read " + length + " bytes");
	
	byte[] physMemory = Machine.processor().getMemory();
	if ((vaddr < 0) || (vaddr >= physMemory.length)) {
		return 0;
	}
	int currentAmount = 0;
	for (int i = 0; i < length; i++) {
    	int vpn = Machine.processor().pageFromAddress(vaddr+i);
    	int pageOffset = Machine.processor().offsetFromAddress(vaddr+i);
    	if (vpn >= pageTable.length) {
    	    //invalid page!
    	    //System.out.println("Invalid page: "+vpn);
    	    return currentAmount; //this line is a placeholder, in reality we should kill the process
    	}
    	TranslationEntry mapping = pageTable[vpn];
		int physPageNum = mapping.ppn;
		mapping.used = true;
		if (!mapping.valid) {
    		return currentAmount;
    	}
		int physAddr = Machine.processor().makeAddress(physPageNum, pageOffset);
		if (physAddr > physMemory.length) {
			return currentAmount;
		}
		//System.out.println("Reading from address " + physAddr + " to data index " + i);
		data[i+offset] = physMemory[physAddr];
		currentAmount++;
	}
//	System.out.println("Read " + currentAmount + " bytes");
	return currentAmount;
	}

    /**
     * Transfer all data from the specified array to this process's virtual
     * memory.
     * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param   vaddr   the first byte of virtual memory to write.
     * @param   data    the array containing the data to transfer.
     * @return  the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data) {
        return writeVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from the specified array to this process's virtual memory.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param   vaddr   the first byte of virtual memory to write.
     * @param   data    the array containing the data to transfer.
     * @param   offset  the first byte to transfer from the array.
     * @param   length  the number of bytes to transfer from the array to
     *                  virtual memory.
     * @return  the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data, int offset,
            int length) {
	//Keep this line
	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

//	System.out.println("Supposed to write " + length + " bytes");
	byte[] physMemory = Machine.processor().getMemory();
	if ((vaddr < 0) || (vaddr >= physMemory.length)) {
		return 0;
	}
	int currentAmount = 0;
	
	for (int i = 0; i < length; i++) {
    	int vpn = Machine.processor().pageFromAddress(vaddr+i);
    	int pageOffset = Machine.processor().offsetFromAddress(vaddr+i);
    	if (vpn >= pageTable.length) {
    	    //invalid page!
//    	    System.out.println("Invalid page: "+vpn);
    	    return currentAmount; //this line is a placeholder, in reality we should kill the process
    	}
    	TranslationEntry mapping = pageTable[vpn];
    	if (!mapping.valid) {
    		return currentAmount;
    	}
		int physPageNum = mapping.ppn;
		int physAddr = Machine.processor().makeAddress(physPageNum, pageOffset);
		if (physAddr > physMemory.length) {
			return currentAmount;
		}
		// Only write if section is not read only
		if (!mapping.readOnly) {
			physMemory[physAddr] = data[i];
			mapping.used = true;
			mapping.dirty = true;
			currentAmount++;
		} else {
//		    System.out.println("Trying to write to read only");
		    return currentAmount;
		}
	}
//	System.out.println("Wrote " + currentAmount + "bytes");
	return currentAmount;
	}

    /**
     * Load the executable with the specified name into this process, and
     * prepare to pass it the specified arguments. Opens the executable, reads
     * its header information, and copies sections and arguments into this
     * process's virtual memory.
     *
     * @param   name    the name of the file containing the executable.
     * @param   args    the arguments to pass to the executable.
     * @return  <tt>true</tt> if the executable was successfully loaded.
     */
    private boolean load(String name, String[] args) {
        Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");

        OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
        if (executable == null) {
            Lib.debug(dbgProcess, "\topen failed");
            return false;
        }

        try {
            coff = new Coff(executable);
        }
        catch (EOFException e) {
            executable.close();
            Lib.debug(dbgProcess, "\tcoff load failed");
            return false;
        }

        // make sure the sections are contiguous and start at page 0
        numPages = 0;
        for (int s=0; s<coff.getNumSections(); s++) {
            CoffSection section = coff.getSection(s);
            if (section.getFirstVPN() != numPages) {
                coff.close();
                Lib.debug(dbgProcess, "\tfragmented executable");
                return false;
            }
            numPages += section.getLength();
        }

        // make sure the argv array will fit in one page
        byte[][] argv = new byte[args.length][];
        int argsSize = 0;
        for (int i=0; i<args.length; i++) {
            argv[i] = args[i].getBytes();
            // 4 bytes for argv[] pointer; then string plus one for null byte
            argsSize += 4 + argv[i].length + 1;
        }
        if (argsSize > pageSize) {
            coff.close();
            Lib.debug(dbgProcess, "\targuments too long");
            return false;
        }

        // program counter initially points at the program entry point
        initialPC = coff.getEntryPoint();

        // next comes the stack; stack pointer initially points to top of it
        numPages += stackPages;
        initialSP = numPages*pageSize;

        // and finally reserve 1 page for arguments
        numPages++;

        if (!loadSections())
            return false;

        activeLock.acquire();
        active++;
        activeLock.release();

        counterLock.acquire();
        pid = counter++;
        counterLock.release();

        // store arguments in last page
        int entryOffset = (numPages-1)*pageSize;
        int stringOffset = entryOffset + args.length*4;

        this.argc = args.length;
        this.argv = entryOffset;

        for (int i=0; i<argv.length; i++) {
            byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
            Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
            entryOffset += 4;
            Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
                       argv[i].length);
            stringOffset += argv[i].length;
            Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
            stringOffset += 1;
        }

        return true;
    }

    /**
     * Allocates memory for this process, and loads the COFF sections into
     * memory. If this returns successfully, the process will definitely be
     * run (this is the last step in process initialization that can fail).
     *
     * @return  <tt>true</tt> if the sections were successfully loaded.
     */
    protected boolean loadSections() {
    	//Keep this section
        if (numPages > Machine.processor().getNumPhysPages()) {
            coff.close();
            Lib.debug(dbgProcess, "\tinsufficient physical memory");
            return false;
        }
        ArrayList<int[]> coffPageMappings = new ArrayList<int[]>();
        ArrayList<Integer> coffVPNs = new ArrayList<Integer>();

        UserKernel.pageLock.acquire();
        LinkedList<Integer> freePages = UserKernel.getFreePages();
        
        //Don't even start loading pages if it won't fit.
        if (freePages.size() < numPages) {
//        	System.out.println("Not enough mem");
        	UserKernel.pageLock.release();
        	return false;
        }
        ArrayList<Integer> readOnlyPages = new ArrayList<Integer>();
        
        for (int i = 0; i < coff.getNumSections(); i++) {
        	CoffSection section = coff.getSection(i);
        	//System.out.println("Initializing " + section.getName() + " section (" + section.getLength() + " pages)");
        	boolean sectionReadOnly = section.isReadOnly();
        	for (int j = 0; j < section.getLength(); j++) {
        		int vpn = section.getFirstVPN()+j; //j = Page Number
        		int ppn = freePages.pop();
        		int[] coffMapping = new int[2];
        		coffMapping[0] = vpn;
        		coffMapping[1] = ppn;
        		coffPageMappings.add(coffMapping);
        		coffVPNs.add(vpn);
//        		System.out.println("Adding a mapping for " + section.getName() + "-> " + vpn + ":" + ppn);
        		if (sectionReadOnly) {
        			readOnlyPages.add(ppn);
//        			System.out.println("Adding " + ppn + " to read-only");
        		}
        		section.loadPage(j, ppn);
        		if (freePages.size() <= 0) {
        			coff.close();
        			Lib.debug(dbgProcess, "\tinsufficient physical memory");
        			UserKernel.pageLock.release();
        			return false;
        		}
        	}
        }
//        System.out.println(readOnlyPages);
        int numPhysPages = Machine.processor().getNumPhysPages();
        pageTable = new TranslationEntry[numPages];
        for (int i = 0; i < numPages; i++) {
        	if (coffVPNs.contains(i)) {
        		int[] mapping = coffPageMappings.get(coffVPNs.indexOf(i));
//        		System.out.println("Mapping virtual page " + mapping[0] + " to phys page " + mapping[1] + " from coffMapping");
        		pageTable[mapping[0]] = new TranslationEntry(mapping[0], mapping[1], true, readOnlyPages.contains(mapping[1]), false, false);
        	} else {
	        	int ppn = freePages.pop();
//	        	System.out.println("Mapping virtual page " + i + " to phys page " + ppn);
	        	pageTable[i] = new TranslationEntry(i, ppn, true, false, false, false);
        	}
        }
        UserKernel.pageLock.release();
        return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
    	UserKernel.pageLock.acquire();
    	LinkedList<Integer> freePages = UserKernel.getFreePages();
    	for (int i = 0; i < numPages; i++) {
    		freePages.add(pageTable[i].ppn);
    	}
//    	System.out.println("Freeing up pages. Number of free pages now: " + freePages.size());
//    	System.out.println(freePages);
    	UserKernel.pageLock.release();
    }

    /**
     * Initialize the processor's registers in preparation for running the
     * program loaded into this process. Set the PC register to point at the
     * start function, set the stack pointer register to point at the top of
     * the stack, set the A0 and A1 registers to argc and argv, respectively,
     * and initialize all other registers to 0.
     */
    public void initRegisters() {
        Processor processor = Machine.processor();

        // by default, everything's 0
        for (int i=0; i<processor.numUserRegisters; i++)
            processor.writeRegister(i, 0);

        // initialize PC and SP according
        processor.writeRegister(Processor.regPC, initialPC);
        processor.writeRegister(Processor.regSP, initialSP);

        // initialize the first two argument registers to argc and argv
        processor.writeRegister(Processor.regA0, argc);
        processor.writeRegister(Processor.regA1, argv);
    }

    /**
     * SYSCALL-RELATED STUFF BEGINS HERE
     */

    private static final int
        syscallHalt = 0,
        syscallExit = 1,
        syscallExec = 2,
        syscallJoin = 3,
        syscallCreate = 4,
        syscallOpen = 5,
        syscallRead = 6,
        syscallWrite = 7,
        syscallClose = 8,
        syscallUnlink = 9;

    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The
     * <i>syscall</i> argument identifies which syscall the user executed:
     *
     * <table>
     * <tr><td>syscall#</td><td>syscall prototype</td></tr>
     * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
     * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
     * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
     *                                                          </tt></td></tr>
     * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
     * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
     * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
     * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
     *                                                          </tt></td></tr>
     * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
     *                                                          </tt></td></tr>
     * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
     * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
     * </table>
     *
     * @param   syscall the syscall number.
     * @param   a0      the first syscall argument.
     * @param   a1      the second syscall argument.
     * @param   a2      the third syscall argument.
     * @param   a3      the fourth syscall argument.
     * @return  the value to be returned to the user.
     */
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
        switch (syscall) {
        case syscallHalt:
            this.handleHalt();
            return -1;
        case syscallExit:
            this.handleExit(a0, true);
            break;
        case syscallExec:
            return this.handleExec(a0, a1, a2);
        case syscallJoin:
            return this.handleJoin(a0, a1);
        case syscallCreate:
            return this.handleCreate(a0);
        case syscallOpen:
            return this.handleOpen(a0);
        case syscallRead:
            return this.handleRead(a0, a1, a2);
        case syscallWrite:
            return this.handleWrite(a0, a1, a2);
        case syscallClose:
            return this.handleClose(a0);
        case syscallUnlink:
            return this.handleUnlink(a0);
        default:
            Lib.debug(dbgProcess, "Unknown syscall " + syscall);
            this.kill();
        }
        return 0;
    }

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param   cause   the user exception that occurred.
     */
    public void handleException(int cause) {
        Processor processor = Machine.processor();
        switch (cause) {
        case Processor.exceptionSyscall:
            int result = handleSyscall(processor.readRegister(Processor.regV0),
                                       processor.readRegister(Processor.regA0),
                                       processor.readRegister(Processor.regA1),
                                       processor.readRegister(Processor.regA2),
                                       processor.readRegister(Processor.regA3));
            processor.writeRegister(Processor.regV0, result);
            processor.advancePC();
            break;
        default:
            Lib.debug(dbgProcess, "Unexpected exception: " +
                      Processor.exceptionNames[cause]);
            this.kill();
        }
    }

    /**
     * Handle the halt() system call.
     */
    protected void handleHalt() {
        if (this.pid == ROOT) {
            Machine.halt();
            Lib.assertNotReached("Machine.halt() did not halt machine!");
        }
    }

    protected int handleCreate(int nameAddr) {
        return handleCreateOrOpen(nameAddr, true);
    }

    protected int handleOpen(int nameAddr) {
        return handleCreateOrOpen(nameAddr, false);
    }

    protected int handleCreateOrOpen(int nameAddr, boolean create) {
        if (this.bogusArguments(nameAddr, 0, 0)) {
            return -1;
        } else if (this.descriptors.isEmpty()) {
            return -1;
        }
        String name = this.readVirtualMemoryString(nameAddr, MAX_FILENAME_SIZE);
        if (name == null) {
            return -1;
        }
        OpenFile newFile = UserKernel.fileSystem.open(name, create);
        if (newFile == null) {
            return -1;
        }
        int newDescriptor = this.descriptors.poll();
        Lib.assertTrue(this.files[newDescriptor] == null);
        this.files[newDescriptor] = newFile;
        return newDescriptor;
    }

    protected int handleRead(int fileDesc, int buffAddr, int count) {
        if (this.bogusArguments(buffAddr, fileDesc, count)) {
            return -1;
        } else if (this.files[fileDesc] == null) {
            return -1;
        }
        byte[] kernelCopy = new byte[count];
        int bytesReadFromFile = this.files[fileDesc].read(kernelCopy, 0, count);
        if (bytesReadFromFile < 0) {
            return -1;
        }
        int bytesWrittenToMem = this.writeVirtualMemory(buffAddr, kernelCopy, 0, bytesReadFromFile);
        if (bytesWrittenToMem < bytesReadFromFile) { // TODO: do we return -1 or bytesWritten?
            return -1;
        }
        return bytesReadFromFile;
    }

    protected int handleWrite(int fileDesc, int buffAddr, int count) {
        if (this.bogusArguments(buffAddr, fileDesc, count)) {
            return -1;
        } else if (this.files[fileDesc] == null) {
            return -1;
        }
        byte[] kernelCopy = new byte[count];
        int bytesReadFromMem = this.readVirtualMemory(buffAddr, kernelCopy);
        if (bytesReadFromMem < count) {
            return -1;
        }
        int bytesWrittenToFile = this.files[fileDesc].write(kernelCopy, 0, bytesReadFromMem);
        if (bytesWrittenToFile < 0) {
            return -1;
        }
        return bytesWrittenToFile;
    }

    protected int handleClose(int fileDesc) {
        if (this.bogusArguments(0, fileDesc, 0)) {
            return -1;
        } else if (this.files[fileDesc] == null) {
            return -1;
        }
        this.files[fileDesc].close();
        this.files[fileDesc] = null;
        this.descriptors.offer(fileDesc);
        return 0;
    }

    protected int handleUnlink(int nameAddr) {
        if (this.bogusArguments(nameAddr, 0, 0)) {
            return -1;
        }
        String name = this.readVirtualMemoryString(nameAddr, MAX_FILENAME_SIZE);
        if (name == null) {
            return -1;
        }
        if (UserKernel.fileSystem.remove(name)) {
            return 0;
        } else {
            return -1;
        }
    }

    protected int handleExec(int name, int argc, int argv) {
        UserProcess proc = UserProcess.newUserProcess();
        String nameString = readVirtualMemoryString(name, MAX_FILENAME_SIZE);
        int[] argPointers = new int[argc];
        byte[] temp;
        for (int i = 0; i < argc; i++) {
            temp = new byte[4];
            readVirtualMemory(argv + (4 * i), temp);
            argPointers[i] = Lib.bytesToInt(temp, 0);
        }
        String[] args = new String[argPointers.length];
        for (int i = 0; i < argc; i++) {
            args[i] = readVirtualMemoryString(argPointers[i],
                                              MAX_FILENAME_SIZE);
        }
        if (proc.execute(nameString, args)) {
            childLock.acquire();
            children.add(proc.pid);
            childLock.release();
            processesLock.acquire();
            processes.put(proc.pid, proc);
            processesLock.release();
            return proc.pid;
        } else {
            return -1;
        }
    }

    protected int handleJoin(int pid, int status) {
        childLock.acquire();
        if (!children.contains(pid)) {
            childLock.release();
            return -1;
        }
        childLock.release();
        processesLock.acquire();
        UserProcess joining = processes.get(pid);
        processesLock.release();
        UThread t = joining.thread;
        if (t != null) {
            t.join();
        }
        byte[] data = new byte[WORD_LENGTH];
        statusLock.acquire();
        int pidToUse = statuses.get(pid);
        statusLock.release();
        Lib.bytesFromInt(data, 0, pidToUse);
        writeVirtualMemory(status, data);
        if (joining.exitedNormally) {
            return 1;
        }
        return 0;
    }

    protected void handleExit(int status, boolean normal) {
        exitedNormally = normal;
        unloadSections();
        activeLock.acquire();
        if (--active == 0) {
            Kernel.kernel.terminate();
        }
        activeLock.release();
        statusLock.acquire();
        statuses.put(pid, status);
        statusLock.release();
        thread = null;
        for (int i = 0; i < FILES; i++) {
            handleClose(i);
        }
        UThread.finish();
    }

    private void kill() {
        this.handleExit(-1, false);
    }

    protected boolean bogusArguments(int pointer, int fileDesc, int count) {
        return (pointer<0 || count<0 || fileDesc<0 || fileDesc>=FILES);
    }

    /**
     * SYSCALL-RELATED STUFF ENDS HERE
     */
}