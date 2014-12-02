/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.javafx.tools.packager.bundlers;

import com.sun.javafx.tools.packager.Log;
import com.sun.javafx.tools.resource.linux.LinuxResources;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class LinuxRPMBundler extends Bundler {
    LinuxAppBundler appBundler = new LinuxAppBundler();
    BundleParams params;
    private File configRoot = null;
    File imageDir = null;
    private boolean menuShortcut = false;
    private boolean desktopShortcut = false;

    private final static String DEFAULT_ICON = "javalogo_white_32.png";
    private final static String DEFAULT_SPEC_TEMPLATE = "template.spec";
    private final static String DEFAULT_DESKTOP_FILE_TEMPLATE = "template.desktop";

    private final static String TOOL_RPMBUILD = "rpmbuild";

    public LinuxRPMBundler() {
        super();
        baseResourceLoader = LinuxResources.class;
    }

    @Override
    protected void setBuildRoot(File dir) {
        super.setBuildRoot(dir);
        configRoot = new File(dir, "linux");
        configRoot.mkdirs();
        appBundler.setBuildRoot(dir);
    }

    @Override
    public void setVerbose(boolean m) {
        super.setVerbose(m);
        appBundler.setVerbose(m);
    }

    private boolean testTool(String toolName, String minVersion) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                toolName,
                "--version");
            IOUtils.exec(pb, Log.isDebug(), true); //not interested in the output
            //TODO: Version is ignored; need to extract version string and compare!
        } catch (Exception e) {
            Log.verbose("Test for ["+toolName+"]. Result: "+e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    boolean validate(BundleParams p) throws Bundler.UnsupportedPlatformException, Bundler.ConfigException {
        if (!(p.type == Bundler.BundleType.ALL || p.type == Bundler.BundleType.INSTALLER)
                 || !(p.bundleFormat == null || "rpm".equals(p.bundleFormat))) {
            return false;
        }
        //run basic validation to ensure requirements are met
        //we are not interested in return code, only possible exception
        appBundler.doValidate(p);

        //TODO: validate presense of required tools?
        if (!testTool(TOOL_RPMBUILD, "4")){
            throw new Bundler.ConfigException(
                    "Can not find rpmbuild.",
                    "  Install packages needed to build RPM.");
        }

        return true;
    }

    private boolean prepareProto() {
        if (!appBundler.doBundle(params, imageDir, true)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean bundle(BundleParams p, File outdir) {
        imageDir = new File(imagesRoot, "linux-rpm.image");
        try {
            params = p;

            imageDir.mkdirs();

            menuShortcut = params.needMenu;
            desktopShortcut = params.needShortcut;
            if (!menuShortcut && !desktopShortcut) {
               //both can not be false - user will not find the app
               Log.verbose("At least one type of shortcut is required. Enabling menu shortcut.");
               menuShortcut = true;
            }

            if (prepareProto() && prepareProjectConfig()) {
                return buildRPM(outdir);
            }
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                if (verbose) {
                    saveConfigFiles();
                }
                if (imageDir != null && !Log.isDebug()) {
                    IOUtils.deleteRecursive(imageDir);
                } else if (imageDir != null) {
                    Log.info("Kept working directory for debug: "
                            + imageDir.getAbsolutePath());
                }
             } catch (FileNotFoundException ex) {
                return false;
            }
        }
    }

    protected void saveConfigFiles() {
        try {
            if (getConfig_SpecFile().exists()) {
                IOUtils.copyFile(getConfig_SpecFile(),
                        new File(configRoot, getConfig_SpecFile().getName()));
            }
            if (getConfig_DesktopShortcutFile().exists()) {
                IOUtils.copyFile(getConfig_DesktopShortcutFile(),
                        new File(configRoot, getConfig_DesktopShortcutFile().getName()));
            }
            if (getConfig_IconFile().exists()) {
                IOUtils.copyFile(getConfig_IconFile(),
                        new File(configRoot, getConfig_IconFile().getName()));
            }
            Log.info("  Config files are saved to "
                    + configRoot.getAbsolutePath()
                    + ". Use them to customize package.");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "RPM bundler";
    }

    private String getLicenseFileString() {
        StringBuilder sb = new StringBuilder();
        for (String f: params.licenseFile) {
            if (sb.length() != 0) {
                sb.append("\n");
            }
            sb.append("%doc /opt/");
            sb.append(getBundleName());
            sb.append("/app/");
            sb.append(f);
        }
        return sb.toString();
    }

    private String getBundleName() {
        String nm;
        if (params.name != null) {
            nm = params.name;
        } else {
            nm = params.getMainClassName();
        }
        //spaces are not allowed (in RPM package names)
        nm = nm.replaceAll(" ", "");
        return nm;
    }

    private String getVersion() {
        if (params.appVersion != null) {
            return params.appVersion;
        } else {
            return "1.0";
        }
    }

    private boolean prepareProjectConfig() throws IOException {
        Map<String, String> data = new HashMap<String, String>();

        data.put("APPLICATION_NAME", getBundleName());
        data.put("APPLICATION_VENDOR", params.vendor != null ? params.vendor : "Unknown");
        data.put("APPLICATION_VERSION", getVersion());
        data.put("APPLICATION_LAUNCHER_FILENAME",
                appBundler.getLauncher(imageDir, params).getName());
        data.put("APPLICATION_DESKTOP_SHORTCUT",
                desktopShortcut ? "returnTrue" : "returnFalse");
        data.put("APPLICATION_MENU_SHORTCUT",
                menuShortcut ? "returnTrue" : "returnFalse");
        data.put("DEPLOY_BUNDLE_CATEGORY",
                params.applicationCategory != null ?
                  params.applicationCategory : "Applications;");
        data.put("APPLICATION_DESCRIPTION",
                params.description != null ?
                   params.description : params.name);
        data.put("APPLICATION_SUMMARY",
                params.title != null ?
                   params.title : params.name);
        data.put("APPLICATION_LICENSE_TYPE",
                params.licenseType != null ? params.licenseType : "unknown");
        data.put("APPLICATION_LICENSE_FILE", getLicenseFileString());

        //prepare spec file
        Writer w = new BufferedWriter(new FileWriter(getConfig_SpecFile()));
        String content = preprocessTextResource(
                LinuxAppBundler.LINUX_BUNDLER_PREFIX + getConfig_SpecFile().getName(),
                "RPM spec file", DEFAULT_SPEC_TEMPLATE, data);
        w.write(content);
        w.close();

        //prepare desktop shortcut
        w = new BufferedWriter(new FileWriter(getConfig_DesktopShortcutFile()));
        content = preprocessTextResource(
                LinuxAppBundler.LINUX_BUNDLER_PREFIX + getConfig_DesktopShortcutFile().getName(),
                "Menu shortcut descriptor", DEFAULT_DESKTOP_FILE_TEMPLATE, data);
        w.write(content);
        w.close();

        //prepare installer icon
        File iconTarget = getConfig_IconFile();
        if (params.icon == null || !params.icon.exists()) {
            fetchResource(LinuxAppBundler.LINUX_BUNDLER_PREFIX + iconTarget.getName(),
                    "menu icon",
                    DEFAULT_ICON,
                    iconTarget);
        } else {
            fetchResource(LinuxAppBundler.LINUX_BUNDLER_PREFIX + iconTarget.getName(),
                    "menu icon",
                    params.icon,
                    iconTarget);
        }

        return true;
    }

    private File getConfig_DesktopShortcutFile() {
        return new File(appBundler.getLauncher(imageDir, params).getParentFile(),
                getBundleName() + ".desktop");
    }

    private File getConfig_IconFile() {
        return new File(appBundler.getLauncher(imageDir, params).getParentFile(),
                getBundleName() + ".png");
    }

    private File getConfig_SpecFile() {
        return new File(imageDir,
                getBundleName() + ".spec");
    }


    private boolean buildRPM(File outdir) throws IOException {
        Log.verbose("Generating RPM for installer to: " + outdir.getAbsolutePath());

        File broot = new File(buildRoot, "rmpbuildroot");

        outdir.mkdirs();

        //run rpmbuild
        ProcessBuilder pb = new ProcessBuilder(
                TOOL_RPMBUILD,
                "-bb", getConfig_SpecFile().getAbsolutePath(),
                "--target", "i686",
//                "--define", "%__jar_repack %{nil}",  //debug: improves build time (but will require unpack to install?)
                "--define", "%_sourcedir "+imageDir.getAbsolutePath(),
                "--define", "%_rpmdir " + outdir.getAbsolutePath(), //save result to output dir
                "--define", "%_topdir " + broot.getAbsolutePath() //do not use other system directories to build as current user
                );
        pb = pb.directory(imageDir);
        IOUtils.exec(pb, verbose);

        IOUtils.deleteRecursive(broot);

        Log.info("Package (.rpm) saved to: " + outdir.getAbsolutePath());

        return true;
    }
}
