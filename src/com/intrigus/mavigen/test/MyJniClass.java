/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
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
 ******************************************************************************/

package com.intrigus.mavigen.test;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.swing.JFrame;

import com.intrigus.mavigen.AntScriptGenerator;
import com.intrigus.mavigen.BuildConfig;
import com.intrigus.mavigen.BuildExecutor;
import com.intrigus.mavigen.BuildTarget;
import com.intrigus.mavigen.JniGenSharedLibraryLoader;
import com.intrigus.mavigen.NativeCodeGenerator;
import com.intrigus.mavigen.BuildTarget.TargetOs;

public class MyJniClass {

	// @off
	/*JNI
	#import <Foundation/Foundation.h>
	#import <AppKit/AppKit.h>
	#import <JavaNativeFoundation/JavaNativeFoundation.h>
	 */
	// @on
	
	static public native void add (int a, int b, ByteBuffer buf); /*
   	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
   	NSLog(@"This is the main program.");
    	NSBeep(); // Works fine.
    	NSBeep(); // Works fine.NSBeep(); // Works fine.NSBeep(); // Works fine.NSBeep(); // Works fine.NSBeep(); // Works fine.
    	[[NSSound soundNamed:@"Frog"] play]; // Works fine.
    	NSLog(@"This is the main program.");
    	[pool drain];
	 */
	static public native String bla(String b, String c); /*MANUAL
	// A jstring container for the output value
    jstring path = NULL;
    
    // Placeholder for the NSString path that will be set inside the block
    __block NSString *nsPath = Nil;;
    
    // Copy the title to an NSString so it an be used safely inside the block
    // even if it is on a different thread
    NSString *nsTitle = @"This is the main program.";
    
    // Copy the extension into an NSString so it can be used safely inside
    // the block even if it is on a different thread
    NSString *cocoaExtension = @".txt";
    
    
    // Create a block for the code that will create and interact with
    // the NSSavePanel so that it can be run on a different thread.  All
    // interaction with the NSSavePanel class needs to be on the main application
    // thread, so if this method is accessed on a different thread (e.g.
    // the AWT event thread, we'll need to block and run this code on the
    // main application thread.
    void (^block)(void);
    block = ^(void){
        // This block's code must ONLY ever be run on the main
        // application thread.
        
        NSSavePanel *panel = [NSSavePanel savePanel];
        NSArray *types = [NSArray arrayWithObjects: cocoaExtension,nil];
        [panel setAllowedFileTypes: types];
        [panel setCanSelectHiddenExtension:TRUE];
        [panel setExtensionHidden:TRUE];
        [panel setTitle: nsTitle];
        if ( [panel runModal] == NSFileHandlingPanelOKButton ){
            // The user clicked OK in the file save dialog, so we
            // now save the user's file path selection in the nsPath.
            NSURL * out = [[panel URL] filePathURL];
            
            // Set the nsPath so that it can be accessed outside this
            // block after it is run.  We call retain on the string
            // so that it won't be destroyed after the block is
            // finished executing.
            nsPath = [[out path] retain];
        }
    };
    
    // Check if this is already running on the main thread.
    if ( [NSThread isMainThread]){
        // We are on the main thread, so we can execute the block directly.
        block();
    } else {
        // We are not on the main thread so we need to run the block on the
        // main thread, and wait for it to complete.        
        [JNFRunLoop performOnMainThreadWaiting:YES withBlock:block];
    }
    
    
    if ( nsPath != nil ){
        // Since nsPath is Not nil, it looks like the user chose a file
        // Copy the NSString path back to the jstring to be returned
        // from the method.
        NSLog(nsPath);
        //path = JNFNSToJavaString(env, nsPath);
        
        // Release the nsPath to prevent memory leak.
        [nsPath release];
    }
    
    // Return the path.  This may be null
    return path;
    
    // Matching the opening JNF_COCOA_ENTER(env) at the beginning of the method.
    //JNF_COCOA_EXIT(env);
    
    // It is necessary to return NULL here in case there was some failure or
    // exception that prevented us from reaching the return statements inside
    // the JNF_COCOA_ENTER/EXIT region.
    return NULL;
    */
	
	public static void main(String[] args) throws Exception {
		// generate C/C++ code
		new NativeCodeGenerator().generate("src", "bin", "jni", new String[] { "**/MyJniClass.java" }, null);
		
		// generate build scripts, for win32 only
		BuildConfig buildConfig = new BuildConfig("test");		
		BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.MacOsX, true);
		//win32.cIncludes = new String[]{"**/*.m"};
		//win32.headerDirs = new String[]{"JavaNativeFoundation.framework"};
		System.out.println(win32.linkerFlags);
		win32.cFlags += " -F/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.11.sdk/System/Library/Frameworks/JavaVM.framework/Frameworks";
		win32.linkerFlags += " -F/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.11.sdk/System/Library/Frameworks/JavaVM.framework/Frameworks -framework Foundation -framework AppKit -framework JavaNativeFoundation"; // 
		//win32.compilerPrefix = "";
		//win32.cppFlags += " -g";
		new AntScriptGenerator().generate(buildConfig, win32);
		
		System.out.println(System.getenv("ANT_HOME"));
		// build natives
		BuildExecutor.executeAnt("jni/build-macosx64.xml", "-v");
			
		// load the test-natives.jar and from it the shared library, then execute the test. 
		//System.loadLibrary("macutil");
		new JniGenSharedLibraryLoader("libs/test-natives.jar").load("test");
		//ByteBuffer buffer = ByteBuffer.allocateDirect(1);
		//buffer.put(0, (byte)8);
		//new JFrame();

		System.out.println(MyJniClass.bla("YOLO", ".txt"));
		//MyJniClass.add(1,2, null);
	}
}