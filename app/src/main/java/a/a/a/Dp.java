package a.a.a;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.android.sdklib.build.ApkBuilder;
import com.android.tools.r8.D8;
import com.besome.sketch.design.DesignActivity;
import com.github.megatronking.stringfog.plugin.StringFogClassInjector;
import com.github.megatronking.stringfog.plugin.StringFogMappingPrinter;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kellinwood.security.zipsigner.ZipSigner;
import kellinwood.security.zipsigner.optional.CustomKeySigner;
import kellinwood.security.zipsigner.optional.KeyStoreFileManager;
import mod.agus.jcoderz.builder.DexMerge;
import mod.agus.jcoderz.command.ProcessingFiles;
import mod.agus.jcoderz.dex.Dex;
import mod.agus.jcoderz.dx.command.dexer.Main;
import mod.agus.jcoderz.dx.merge.CollisionPolicy;
import mod.agus.jcoderz.dx.merge.DexMerger;
import mod.agus.jcoderz.editor.library.ExtLibSelected;
import mod.agus.jcoderz.editor.manage.library.locallibrary.ManageLocalLibrary;
import mod.agus.jcoderz.lib.FilePathUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.alucard.tn.compiler.ResourceCompiler;
import mod.hey.studios.build.BuildSettings;
import mod.hey.studios.project.ProjectSettings;
import mod.hey.studios.project.proguard.ProguardHandler;
import mod.hey.studios.util.SystemLogPrinter;
import mod.jbk.util.LogUtil;
import proguard.ProGuard;

public class Dp {

    public final static String TAG = "AppBuilder";
    /**
     * Default minSdkVersion (?)
     */
    public final String a = "21";
    /**
     * Default targetSdkVersion (?)
     */
    public final String b = "28";
    public final String c = File.separator;
    public final String m = "libs";
    public File aapt2Dir;
    public BuildSettings build_settings;
    public DesignActivity.a buildingDialog;
    /**
     * Command(s) to execute after extracting AAPT/AAPT2 (fill in 2 with the file name before using)
     */
    public String[] d = {"chmod", "744", ""};
    public Context e;
    public yq f;
    public FilePathUtil fpu;
    public oB g;
    /**
     * Directory "tmp" in files directory, where libs are extracted and compiled
     */
    public File h;
    /**
     * File object that represents aapt
     */
    public File i;
    public Fp j;
    /**
     * A StringBuffer with System.err of Eclipse compiler. If compilation succeeds, it doesn't have any content, if it doesn't, there is.
     */
    public StringBuffer k = new StringBuffer();
    /**
     * Extracted built-in libraries directory
     */
    public File l;
    public DexMerge merge;
    public ManageLocalLibrary mll;
    public Kp n;
    public String o;
    public ProguardHandler proguard;
    public ProjectSettings settings;
    ArrayList<String> dexesGenerated;
    ArrayList<String> extraDexes;

    public Dp(Context context, yq yqVar) {
        /*
         * Detect some bad behaviour of the app.
         */
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        );
        /*
         * Start logging to debug.txt stored in /Internal storage/.sketchware/.
         */
        SystemLogPrinter.start();
        e = context;
        f = yqVar;
        g = new oB(false);
        j = new Fp();
        h = new File(context.getFilesDir(), "tmp");
        if (!h.exists()) {
            h.mkdir();
        }
        i = new File(h, "aapt");
        aapt2Dir = new File(h, "aapt2");
        l = new File(context.getFilesDir(), "libs");
        n = new Kp();
        o = new File(l, "android.jar").getAbsolutePath();
        mll = new ManageLocalLibrary(f.b);
        fpu = new FilePathUtil();
        settings = new ProjectSettings(f.b);
        proguard = new ProguardHandler(f.b);
        build_settings = new BuildSettings(f.b);
        o = build_settings.getValue(BuildSettings.SETTING_ANDROID_JAR_PATH, o);
        try {
            Toast.makeText(e.getApplicationContext(), "Hello! This is a test to see if Dp's Context field e is null.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to toast with mDp.e: " + e.getMessage(), e);
        }
    }

    public Dp(DesignActivity.a anA, Context context, yq yqVar) {
        this(context, yqVar);
        buildingDialog = anA;
    }

    /**
     * Compile resources and log time needed.
     *
     * @throws Exception Thrown when anything goes wrong while compiling resources
     */
    public void a() throws Exception {
        long savedTimeMillis = System.currentTimeMillis();
        b();
        Log.d(TAG, "Compiling resources took " + (System.currentTimeMillis() - savedTimeMillis) + " ms");
    }

    public void a(iI iIVar, String str) {
        ZipSigner b2 = iIVar.b(new CB().a(str));
        yq yqVar = f;
        b2.signZip(yqVar.G, yqVar.I);
    }

    public final void a(String str, ArrayList<String> arrayList) throws Exception {
        dexLibraries(str, arrayList);
    }

    /**
     * Checks if the source file in assets has a different size than {@code compareTo}.
     *
     * @param fileInAssets The file in assets whose length to compare
     * @param compareTo    The file whose length to compare
     */
    public final boolean a(String fileInAssets, String compareTo) {
        long length;
        File compareToFile = new File(compareTo);
        long lengthOfFileInAssets = g.a(e, fileInAssets);
        if (compareToFile.exists()) {
            length = compareToFile.length();
        } else {
            length = 0;
        }
        if (lengthOfFileInAssets == length) {
            return false;
        }
        g.a(compareToFile);
        g.a(e, fileInAssets, compareTo);
        return true;
    }

    public void compileWithAapt2() throws zy, IOException {
        LogUtil.dump(TAG, f);
        ResourceCompiler compiler = new ResourceCompiler(
                this,
                aapt2Dir,
                build_settings.getValue(
                        BuildSettings.SETTING_OUTPUT_FORMAT,
                        BuildSettings.SETTING_OUTPUT_FORMAT_APK
                ).equals(BuildSettings.SETTING_OUTPUT_FORMAT_AAB),
                buildingDialog);
        compiler.compile();
        compiler.link();
    }

    /**
     * Compile the project's resources.
     *
     * @throws Exception Thrown in case AAPT/AAPT2 has an error while compiling resources.
     */
    public void b() throws Exception {
        if (build_settings.getValue(
                BuildSettings.SETTING_RESOURCE_PROCESSOR,
                BuildSettings.SETTING_RESOURCE_PROCESSOR_AAPT
        ).equals(BuildSettings.SETTING_RESOURCE_PROCESSOR_AAPT2)) {
            compileWithAapt2();
            return;
        }

        /* Start of generating arguments for AAPT */
        ArrayList<String> args = new ArrayList<>();
        args.add(i.getAbsolutePath());
        args.add("package");
        String extraPackages = e();
        if (!extraPackages.isEmpty()) {
            args.add("--extra-packages");
            args.add(extraPackages);
        }
        args.add("--min-sdk-version");
        args.add(settings.getValue("min_sdk", a));
        args.add("--target-sdk-version");
        args.add(settings.getValue("target_sdk", b));
        args.add("--version-code");
        args.add(((f.l == null) || f.l.isEmpty()) ? "1" : f.l);
        args.add("--version-name");
        args.add(((f.m == null) || f.m.isEmpty()) ? "1.0" : f.m);
        args.add("--auto-add-overlay");
        args.add("--generate-dependencies");
        args.add("-f");
        args.add("-m");
        args.add("--non-constant-id");
        args.add("--output-text-symbols");
        args.add(f.t);
        if (f.N.g) {
            args.add("--no-version-vectors");
        }
        args.add("-S");
        args.add(f.w);
        for (String localLibraryResDirectory : mll.getResLocalLibrary()) {
            args.add("-S");
            args.add(localLibraryResDirectory);
        }
        if (FileUtil.isExistFile(fpu.getPathResource(f.b))) {
            args.add("-S");
            args.add(fpu.getPathResource(f.b));
        }
        args.add("-A");
        args.add(f.A);
        if (FileUtil.isExistFile(fpu.getPathAssets(f.b))) {
            args.add("-A");
            args.add(fpu.getPathAssets(f.b));
        }
        for (String localLibraryAssetsDirectory : mll.getAssets()) {
            args.add("-A");
            args.add(localLibraryAssetsDirectory);
        }
        for (Jp library : n.a()) {
            if (library.d()) {
                String str7 = l.getAbsolutePath() + c + "libs" + c + library.a() + c + "assets";
                args.add("-A");
                args.add(str7);
            }

            if (library.c()) {
                String str6 = l.getAbsolutePath() + c + "libs" + c + library.a() + c + "res";
                args.add("-S");
                args.add(str6);
            }
        }
        args.add("-J");
        args.add(f.v);
        args.add("-G");
        args.add(f.aapt_rules);
        args.add("-M");
        args.add(f.r);
        args.add("-I");
        args.add(o);
        args.add("-F");
        args.add(f.C);
        if (j.a(args.toArray(new String[0])) != 0) {
            throw new zy(j.a.toString());
        }
    }

    public void b(String str, String str2) {
        Security.addProvider(new BouncyCastleProvider());
        CustomKeySigner.signZip(new ZipSigner(), wq.j(), str.toCharArray(), str2, str.toCharArray(), "SHA1WITHRSA", f.G, f.I);
    }

    public boolean isD8Enabled() {
        return build_settings.getValue(
                BuildSettings.SETTING_DEXER,
                BuildSettings.SETTING_DEXER_DX
        ).equals(BuildSettings.SETTING_DEXER_D8);
    }

    public String getDxRunningText() {
        return (isD8Enabled() ? "D8" : "Dx") + " is running...";
    }

    /**
     * Compile Java classes into DEX file(s)
     *
     * @throws Exception Thrown if the compiler has any problems compiling
     */
    public void c() throws Exception {
        if (isD8Enabled()) {
            long savedTimeMillis = System.currentTimeMillis();
            ArrayList<String> args = new ArrayList<>();
            args.add("--release");
            args.add("--intermediate");
            args.add("--min-api");
            args.add(settings.getValue("min_sdk", "21"));
            args.add("--lib");
            args.add(o);
            args.add("--output");
            args.add(f.t);
            if (proguard.isProguardEnabled()) {
                args.add(f.classes_proguard);
            } else {
                args.addAll(ProcessingFiles.getListResource(f.u));
            }
            try {
                Log.d(TAG, "Running D8 with these arguments: " + args.toString());
                D8.main(args.toArray(new String[0]));
                Log.d(TAG, "D8 took " + (System.currentTimeMillis() - savedTimeMillis) + " ms");
            } catch (Exception e) {
                Log.e(TAG, "D8 error: " + e.getMessage(), e);
                throw e;
            }
        } else {
            Main.dexOutputArrays = new ArrayList<>();
            Main.dexOutputFutures = new ArrayList<>();
            long savedTimeMillis = System.currentTimeMillis();
            ArrayList<String> args = new ArrayList<>();
            args.add("--debug");
            args.add("--verbose");
            args.add("--multi-dex");
            args.add("--output=" + f.t);
            if (proguard.isProguardEnabled()) {
                args.add(f.classes_proguard);
            } else {
                args.add(f.u);
            }
            try {
                Log.d(TAG, "Running Dx with these arguments: " + args.toString());
                Main.main(args.toArray(new String[0]));
                Log.d(TAG, "Dx took " + (System.currentTimeMillis() - savedTimeMillis) + " ms");
            } catch (Exception e) {
                Log.e(TAG, "Dx error: " + e.getMessage(), e);
                throw e;
            }
        }
        findExtraDexes();
    }

    public final String d() {
        StringBuilder sb = new StringBuilder();
        sb.append(f.v).append(":").append(o);
        if (!build_settings.getValue(BuildSettings.SETTING_NO_HTTP_LEGACY, "false").equals("true")) {
            sb.append(":");
            sb.append(l.getAbsolutePath());
            sb.append(c);
            sb.append("libs");
            sb.append(c);
            sb.append("http-legacy-android-28");
            sb.append(c);
            sb.append("classes.jar");
        }
        sb.append(":");
        sb.append(l.getAbsolutePath());
        sb.append(c);
        sb.append("jdk");
        sb.append(c);
        sb.append("rt.jar");
        StringBuilder sb2 = new StringBuilder(sb.toString());
        for (Jp next : n.a()) {
            sb2.append(":").append(l.getAbsolutePath()).append(c).append("libs").append(c).append(next.a()).append(c).append("classes.jar");
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append(sb2);
        sb3.append(mll.getJarLocalLibrary());
        if (!build_settings.getValue(BuildSettings.SETTING_CLASSPATH, "").equals("")) {
            sb3.append(":");
            sb3.append(build_settings.getValue(BuildSettings.SETTING_CLASSPATH, ""));
        }
        return sb3.toString();
    }

    public final String classpath() {
        StringBuilder sb = new StringBuilder();
        sb.append(f.v).append(":").append(o);
        if (!build_settings.getValue(BuildSettings.SETTING_NO_HTTP_LEGACY, "false").equals("true")) {
            sb.append(":");
            sb.append(l.getAbsolutePath());
            sb.append(c);
            sb.append("libs");
            sb.append(c);
            sb.append("http-legacy-android-28");
            sb.append(c);
            sb.append("classes.jar");
        }
        sb.append(":");
        sb.append(l.getAbsolutePath());
        sb.append(c);
        sb.append("jdk");
        sb.append(c);
        sb.append("rt.jar");
        StringBuilder sb2 = new StringBuilder(sb.toString());
        for (Jp next : n.a()) {
            sb2.append(":").append(l.getAbsolutePath()).append(c).append("libs").append(c).append(next.a()).append(c).append("classes.jar");
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append(sb2);
        for (HashMap<String, Object> hashMap : mll.list) {
            String obj = hashMap.get("name").toString();
            if (hashMap.containsKey("jarPath") && !proguard.libIsProguardFMEnabled(obj)) {
                sb3.append(":");
                sb3.append(hashMap.get("jarPath").toString());
            }
        }
        if (!build_settings.getValue(BuildSettings.SETTING_CLASSPATH, "").equals("")) {
            sb3.append(":");
            sb3.append(build_settings.getValue(BuildSettings.SETTING_CLASSPATH, ""));
        }
        return sb3.toString();
    }

    public final void dexLibraries(String str, ArrayList<String> arrayList) throws Exception {
        dexesGenerated = new ArrayList<>();
        int findLastDexNo = findLastDexNo();
        ArrayList<Dex> arrayList2 = new ArrayList<>();
        int i2 = 0;
        for (String s : arrayList) {
            Dex dex = new Dex(new FileInputStream(s));
            int size = dex.methodIds().size();
            if (size + i2 >= 65536) {
                mergeDexes(str.replace("classes2.dex", "classes" + findLastDexNo + ".dex"), arrayList2);
                arrayList2.clear();
                arrayList2.add(dex);
                i2 = size;
                findLastDexNo++;
            } else {
                arrayList2.add(dex);
                i2 += size;
            }
        }
        if (arrayList2.size() > 0) {
            mergeDexes(str.replace("classes2.dex", "classes" + findLastDexNo + ".dex"), arrayList2);
        }
    }

    /**
     * Get extra packages used in this project, needed for AAPT/AAPT2.
     */
    public final String e() {
        StringBuilder extraPackages = new StringBuilder();
        for (Jp library : n.a()) {
            if (library.c()) {
                extraPackages.append(library.b()).append(":");
            }
        }
        return extraPackages + mll.getPackageNameLocalLibrary();
    }

    /**
     * Run Eclipse Compiler to compile Java classes from Java source code
     *
     * @throws Throwable Thrown when Eclipse has problems compiling
     */
    public void f() throws Throwable {
        long savedTimeMillis = System.currentTimeMillis();

        class EclipseOutOutputStream extends OutputStream {

            private final StringBuffer mBuffer = new StringBuffer();

            @Override
            public void write(int b) {
                mBuffer.append(b);
            }

            public String getOut() {
                return mBuffer.toString();
            }
        }

        class EclipseErrOutputStream extends OutputStream {

            @Override
            public void write(int b) throws IOException {
                k.append((char) b);
            }
        }

        EclipseOutOutputStream outOutputStream = new EclipseOutOutputStream();
        /* System.out for Eclipse compiler */
        PrintWriter outWriter = new PrintWriter(outOutputStream);

        EclipseErrOutputStream errOutputStream = new EclipseErrOutputStream();
        /* System.err for Eclipse compiler */
        PrintWriter errWriter = new PrintWriter(errOutputStream);

        try {
            ArrayList<String> args = new ArrayList<>();
            args.add("-" + build_settings.getValue(BuildSettings.SETTING_JAVA_VERSION, BuildSettings.SETTING_JAVA_VERSION_1_7));
            args.add("-nowarn");
            if (!build_settings.getValue(BuildSettings.SETTING_NO_WARNINGS, "false").equals("true")) {
                args.add("-deprecation");
            }
            args.add("-d");
            args.add(f.u);
            args.add("-cp");
            args.add(d());
            args.add("-proc:none");
            args.add("-sourcepath");
            args.add(f.y);
            args.add(f.o);
            args.add(f.q);
            if (FileUtil.isExistFile(fpu.getPathJava(f.b))) {
                args.add(fpu.getPathJava(f.b));
            }
            if (FileUtil.isExistFile(fpu.getPathBroadcast(f.b))) {
                args.add(fpu.getPathBroadcast(f.b));
            }
            if (FileUtil.isExistFile(fpu.getPathService(f.b))) {
                args.add(fpu.getPathService(f.b));
            }

            /* Adding built-in libraries' R.java files */
            for (Jp library : n.a()) {
                if (library.c()) {
                    args.add(f.v + File.separator + library.b().replace(".", File.separator) + File.separator + "R.java");
                }
            }

            /* Adding local libraries' R.java files */
            args.addAll(mll.getGenLocalLibrary());

            /* Start compiling */
            org.eclipse.jdt.internal.compiler.batch.Main main = new org.eclipse.jdt.internal.compiler.batch.Main(outWriter, errWriter, false, null, null);
            LogUtil.log(TAG, "Running Eclipse compiler with these arguments: ", "About to log Eclipse compiler's arguments in multiple lines because of length.", args);
            main.compile(args.toArray(new String[0]));

            if (main.globalErrorsCount <= 0) {
                try {
                    outOutputStream.close();
                    errOutputStream.close();
                    outWriter.close();
                    errWriter.close();
                    LogUtil.log(TAG, "System.out of Eclipse compiler: ", "About to log System.out of Eclipse compiler on multiple lines because of length.", outOutputStream.getOut());
                    LogUtil.log(TAG, "System.err of Eclipse compiler: ", "About to log System.err of Eclipse compiler on multiple lines because of length.", k.toString());
                } catch (IOException ignored) {
                }
                Log.d(TAG, "Compiling Java files took " + (System.currentTimeMillis() - savedTimeMillis) + " ms.");
            } else {
                throw new zy(k.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            throw e;
        }
    }

    public final void findExtraDexes() {
        extraDexes = new ArrayList<>();
        ArrayList<String> arrayList = new ArrayList<>();
        FileUtil.listDir(f.t, arrayList);
        for (String str : arrayList) {
            if (str.contains("classes") && str.contains(".dex") && !Uri.parse(str).getLastPathSegment().equals("classes.dex")) {
                extraDexes.add(str);
            }
        }
    }

    public final int findLastDexNo() {
        ArrayList<String> arrayList = new ArrayList<>();
        FileUtil.listDir(f.t, arrayList);
        ArrayList<String> arrayList2 = new ArrayList<>();
        for (String str : arrayList) {
            if (str.contains("classes") && str.contains(".dex")) {
                arrayList2.add(Uri.parse(str).getLastPathSegment());
            }
        }
        if (arrayList2.size() == 1 && arrayList2.get(0).equals("classes.dex")) {
            return 2;
        }
        int i2 = 1;
        for (String str2 : arrayList2) {
            String replace = str2.replace("classes", "").replace(".dex", "");
            i2 = Math.max(i2, replace.isEmpty() ? 1 : java.lang.Integer.parseInt(replace));
        }
        return i2 + 1;
    }

    /**
     * Builds an APK, used when clicking "Run" in DesignActivity
     */
    public void g() {
        ApkBuilder apkBuilder = new ApkBuilder(new File(f.G), new File(f.C), new File(f.E), null, null, System.out);
        for (HashMap<String, Object> localLibraries : mll.list) {
            apkBuilder.addResourcesFromJar(new File(localLibraries.get("jarPath").toString()));
        }
        File file = new File(Environment.getExternalStorageDirectory(),
                ".sketchware/data/".concat(f.b.concat("/files/native_libs")));
        if (FileUtil.isExistFile(file.getAbsolutePath())) {
            apkBuilder.addNativeLibraries(file);
        }
        for (String nativeLibrary : mll.getNativeLibs()) {
            apkBuilder.addNativeLibraries(new File(nativeLibrary));
        }
        for (String extraDex : extraDexes) {
            apkBuilder.addFile(new File(extraDex), Uri.parse(extraDex).getLastPathSegment());
        }
        for (String generatedDex : dexesGenerated) {
            apkBuilder.addFile(new File(generatedDex), Uri.parse(generatedDex).getLastPathSegment());
        }
        apkBuilder.setDebugMode(false);
        apkBuilder.sealApk();
    }

    public String getJava() {
        if (proguard.isProguardEnabled()) {
            ArrayList<String> arrayList = new ArrayList<>();
            FileUtil.listDir(f.classes_proguard, arrayList);
            if (arrayList.size() > 0) {
                return f.classes_proguard;
            }
        }
        return f.u;
    }

    public void h() throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        ArrayList<String> arrayList = new ArrayList<>();
        if (Build.VERSION.SDK_INT <= 23) {
            arrayList.add(l.getAbsolutePath() + c + "dexs" + c + "multidex-2.0.1" + ".dex");
        }
        if (!build_settings.getValue(BuildSettings.SETTING_NO_HTTP_LEGACY, "false").equals("true")) {
            arrayList.add(l.getAbsolutePath() + c + "dexs" + c + "http-legacy-android-28" + ".dex");
        }
        for (Jp next : n.a()) {
            arrayList.add(l.getAbsolutePath() + c + "dexs" + c + next.a() + ".dex");
        }
        for (HashMap<String, Object> hashMap : mll.list) {
            String obj = hashMap.get("name").toString();
            if (hashMap.containsKey("dexPath") && !proguard.libIsProguardFMEnabled(obj)) {
                arrayList.add(hashMap.get("dexPath").toString());
            }
        }
        arrayList.addAll(mll.getExtraDexes());
        a(f.F, arrayList);
        long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
        Log.d(TAG, "Libraries' DEX files merge took " + currentTimeMillis2 + " ms");
    }

    /**
     * Extracts AAPT binaries (if they need to be extracted)
     *
     * @throws Exception If anything goes wrong while extracting
     */
    public void i() throws Exception {
        String aaptPathInAssets;
        String aapt2PathInAssets;
        if (GB.a().toLowerCase().matches("x86")) {
            aaptPathInAssets = "aapt/aapt-x86";
            aapt2PathInAssets = "aapt/aapt2-x86";
        } else {
            aaptPathInAssets = "aapt/aapt-arm";
            aapt2PathInAssets = "aapt/aapt2-arm";
        }
        try {
            if (a(aaptPathInAssets, i.getAbsolutePath())) {
                d[2] = i.getAbsolutePath();
                j.a(d);
            }
            if (a(aapt2PathInAssets, aapt2Dir.getAbsolutePath())) {
                d[2] = aapt2Dir.getAbsolutePath();
                j.a(d);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            throw new By("Couldn't extract AAPT binaries! Message: " + e.getMessage());
        }
    }

    public void j() {
        /* If l doesn't exist, create it */
        if (!g.e(l.getAbsolutePath())) {
            g.f(l.getAbsolutePath());
        }
        String androidJarPath = new File(l, "android.jar.zip").getAbsolutePath();
        String dexsArchivePath = new File(l, "dexs.zip").getAbsolutePath();
        String libsArchivePath = new File(l, "libs.zip").getAbsolutePath();
        String dexsDirectoryPath = new File(l, "dexs").getAbsolutePath();
        String libsDirectoryPath = new File(l, "libs").getAbsolutePath();
        String testkeyDirectoryPath = new File(l, "testkey").getAbsolutePath();
        if (a(m + File.separator + "android.jar.zip", androidJarPath)) {
            /* Delete android.jar */
            g.c(l.getAbsolutePath() + c + "android.jar");
            new KB().a(androidJarPath, l.getAbsolutePath());
        }
        if (a(m + File.separator + "dexs.zip", dexsArchivePath)) {
            g.b(dexsDirectoryPath);
            g.f(dexsDirectoryPath);
            new KB().a(dexsArchivePath, dexsDirectoryPath);
        }
        if (a(m + File.separator + "libs.zip", libsArchivePath)) {
            g.b(libsDirectoryPath);
            g.f(libsDirectoryPath);
            new KB().a(libsArchivePath, libsDirectoryPath);
        }
        String jdkArchivePathInAssets = m + File.separator + "jdk.zip";
        String jdkArchivePath = new File(l, "jdk.zip").getAbsolutePath();
        /* Check if file size has changed */
        if (a(jdkArchivePathInAssets, jdkArchivePath)) {
            String jdkDirectoryPath = new File(l, "jdk").getAbsolutePath();
            /* Delete the directory? */
            g.b(jdkDirectoryPath);
            /* Create the directories? */
            g.f(jdkDirectoryPath);
            /* Extract the archive to the directory? */
            new KB().a(jdkArchivePath, jdkDirectoryPath);
        }
        String testkeyArchivePathInAssets = m + File.separator + "testkey.zip";
        String testkeyArchivePath = new File(l, "testkey.zip").getAbsolutePath();
        if (a(testkeyArchivePathInAssets, testkeyArchivePath)) {
            /* We need to copy testkey.zip to filesDir */
            g.b(testkeyDirectoryPath);
            g.f(testkeyDirectoryPath);
            new KB().a(testkeyArchivePath, testkeyDirectoryPath);

        }
        if (f.N.g) {
            n.a("appcompat-1.0.0");
            n.a("coordinatorlayout-1.0.0");
            n.a("material-1.0.0");
        }
        if (f.N.h) {
            n.a("firebase-common-19.0.0");
        }
        if (f.N.i) {
            n.a("firebase-auth-19.0.0");
        }
        if (f.N.j) {
            n.a("firebase-database-19.0.0");
        }
        if (f.N.k) {
            n.a("firebase-storage-19.0.0");
        }
        if (f.N.m) {
            n.a("play-services-maps-17.0.0");
        }
        if (f.N.l) {
            n.a("play-services-ads-18.2.0");
        }
        if (f.N.o) {
            n.a("gson-2.8.0");
        }
        if (f.N.n) {
            n.a("glide-4.11.0");
        }
        if (f.N.p) {
            n.a("okhttp-3.9.1");
        }
        ExtLibSelected.a(f.N.x, n);
    }

    /**
     * Sign the APK file with testkey.
     */
    public boolean k() {
        ZipSigner zipSigner = new ZipSigner();
        zipSigner.addAutoKeyObserver(new Cp(this));
        KeyStoreFileManager.setProvider(new BouncyCastleProvider());
        zipSigner.setKeymode("testkey");
        zipSigner.signZip(f.G, f.H);
        return true;
    }

    public final void mergeDexes(String target, ArrayList<Dex> dexes) throws Exception {
        new DexMerger(dexes.toArray(new Dex[0]), CollisionPolicy.KEEP_FIRST).merge().writeTo(new File(target));
        dexesGenerated.add(target);
    }

    /**
     * Adds all built-in libraries' ProGuard rules to {@code args}, if any.
     *
     * @param args List of arguments to add built-in libraries' ProGuard roles to.
     */
    public void proguardAddLibConfigs(List<String> args) {
        for (Jp jp : n.a()) {
            String str = l.getAbsolutePath() + c + jp.a() + c + "proguard.txt";
            if (FileUtil.isExistFile(str)) {
                args.add("-include");
                args.add(str);
            }
        }
    }

    /**
     * Generates default ProGuard R.java rules and adds them to {@code args}.
     *
     * @param args List of arguments to add R.java rules to.
     */
    public void proguardAddRjavaRules(List<String> args) {
        StringBuilder sb = new StringBuilder("# R.java rules");
        for (Jp jp : n.a()) {
            if (jp.c() && !jp.b().isEmpty()) {
                sb.append("\n");
                sb.append("-keep class ");
                sb.append(jp.b());
                sb.append(".** { *; }");
            }
        }
        for (HashMap<String, Object> hashMap : mll.list) {
            String obj = hashMap.get("name").toString();
            if (hashMap.containsKey("packageName") && !proguard.libIsProguardFMEnabled(obj)) {
                sb.append("\n");
                sb.append("-keep class ");
                sb.append(hashMap.get("packageName").toString());
                sb.append(".** { *; }");
            }
        }
        sb.append("\n");
        FileUtil.writeFile(f.rules_generated, sb.toString());
        args.add("-include");
        args.add(f.rules_generated);
    }

    public void runProguard() {
        long savedTimeMillis = System.currentTimeMillis();
        ArrayList<String> args = new ArrayList<>();

        /* Include global ProGuard rules */
        args.add("-include");
        args.add(ProguardHandler.ANDROID_PROGUARD_RULES_PATH);

        /* Include ProGuard rules generated by AAPT/AAPT2 */
        args.add("-include");
        args.add(f.aapt_rules);

        /* Include custom ProGuard rules */
        args.add("-include");
        args.add(proguard.getCustomProguardRules());

        proguardAddLibConfigs(args);
        proguardAddRjavaRules(args);

        /* Include local libraries' ProGuard rules */
        for (String localLibraryProGuardRule : mll.getPgRules()) {
            args.add("-include");
            args.add(localLibraryProGuardRule);
        }

        /* Include compiled Java classes (?) IT SAYS -in*jar*s, so why include .class es? */
        args.add("-injars");
        args.add(f.u);

        for (HashMap<String, Object> hashMap : mll.list) {
            String obj = hashMap.get("name").toString();
            if (hashMap.containsKey("jarPath") && proguard.libIsProguardFMEnabled(obj)) {
                args.add("-injars");
                args.add(hashMap.get("jarPath").toString());
            }
        }
        args.add("-libraryjars");
        args.add(classpath());
        args.add("-outjars");
        args.add(f.classes_proguard);
        if (proguard.isDebugFilesEnabled()) {
            args.add("-printseeds");
            args.add(f.printseeds);
            args.add("-printusage");
            args.add(f.printusage);
            args.add("-printmapping");
            args.add(f.printmapping);
        }
        LogUtil.log(TAG, "About to run ProGuard with these arguments: ", "About to log ProGuard's arguments on multiple lines because of length.", args);
        ProGuard.main(args.toArray(new String[0]));
        Log.d(TAG, "ProGuard took " + (System.currentTimeMillis() - savedTimeMillis) + " ms.");
    }

    public void runStringfog() {
        try {
            StringFogMappingPrinter stringFogMappingPrinter = new StringFogMappingPrinter(new File(f.t,
                    "stringFogMapping.txt"));
            StringFogClassInjector stringFogClassInjector = new StringFogClassInjector(new String[0],
                    "UTF-8",
                    "com.github.megatronking.stringfog.xor.StringFogImpl",
                    "com.github.megatronking.stringfog.xor.StringFogImpl",
                    stringFogMappingPrinter);
            stringFogMappingPrinter.startMappingOutput();
            stringFogMappingPrinter.ouputInfo("UTF-8", "com.github.megatronking.stringfog.xor.StringFogImpl");
            stringFogClassInjector.doFog2ClassInDir(new File(f.u));
            KB.a(e, "stringfog/stringfog.zip", f.u);
        } catch (Exception e) {
            Log.e("Stringfog", e.toString());
        }
    }
}