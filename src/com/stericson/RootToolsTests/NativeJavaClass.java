/*
 * This file is part of the RootTools Project: http://code.google.com/p/roottools/
 *
 * Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Dominik Schuermann, Adam Shanks
 *
 * This code is dual-licensed under the terms of the Apache License Version 2.0 and
 * the terms of the General Public License (GPL) Version 2.
 * You may use this code according to either of these licenses as is most appropriate
 * for your project on a case-by-case basis.
 *
 * The terms of each license can be found in the root directory of this project's repository as well as at:
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
 * * http://www.gnu.org/licenses/gpl-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under these Licenses is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See each License for the specific language governing permissions and
 * limitations under that License.
 */

package com.stericson.RootToolsTests;

import com.stericson.RootTools.containers.RootClass;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RootClass.Candidate
public class NativeJavaClass {

    public NativeJavaClass(RootClass.RootArgs args) {
        System.out.println("NativeJavaClass says: oh hi there.");
        String p = "/data/data/com.android.browser/cache";
        File f = new File(p);
        String[] fl = f.list();
        if (fl != null) {
            System.out.println("Look at all the stuff in your browser's cache:");
            for (String af : fl) {
                System.out.println("-" + af);
            }
            System.out.println("Leaving my mark for posterity...");
            File f2 = new File(p + "/roottools_was_here");
            try {
                FileWriter filewriter = new FileWriter(f2);
                BufferedWriter out = new BufferedWriter(filewriter);
                out.write("This is just a file created using RootTool's Sanity check tools..\n");
                out.close();
                System.out.println("Done!");
            } catch (IOException e) {
                System.out.println("...and I failed miserably.");
                e.printStackTrace();
            }

        }
    }

}
