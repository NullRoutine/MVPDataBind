package com.twq.databind;

import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiJavaFile;
import org.apache.http.util.TextUtils;

import java.io.IOException;

public class MVPDataBind extends AnAction {
    private ClassModel classModel;
    private Editor editor;
    private String content;
    private String path;
    private final int MODE_CONTRACT = 0;
    private AnActionEvent e;
    private String packageName;
    private FileGenerator fileGenerator;
    private String className;
    protected PsiDirectory myCurrentDir;//the base line of dir generation
    protected PsiDirectory myContractDir;
    protected PsiDirectory myModelDir;
    protected PsiDirectory myPresenterDir;

    @Override
    public void update(AnActionEvent e) {
        super.update(e);

    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        this.e = e;
        this.editor = (Editor) e.getData(PlatformDataKeys.EDITOR);
        this.classModel = new ClassModel();
        PsiJavaFile javaFile = (PsiJavaFile) e.getData(CommonDataKeys.PSI_FILE);
        assert javaFile != null;
        this.packageName = javaFile.getPackageName().replaceAll(":", ".");
        System.out.println("current package name is :" + this.packageName);
        if (checkCanUse()) {
            createContractContent();
            String fileName = javaFile.getName();
//            className = fileName.substring(0, fileName.indexOf(".java"));//注意截取
            className = classModel.getFunctionName();
            myCurrentDir = javaFile.getContainingFile().getParent();
            fileGenerator = new FileGenerator(e.getProject(), className);
            //生成Model文件
            fileGenerator.generateFile(myCurrentDir, className + "Model", JavaTemplateUtil.INTERNAL_CLASS_TEMPLATE_NAME, new FileGenerator.onFileGeneratedListener() {
                @Override
                public void onJavaFileGenerated(PsiJavaFile javaFile, PsiClass psiClass) {
                    PsiClass contractClass = fileGenerator.myShortNamesCache.getClassesByName(fileGenerator.myPrefix + "Contract", fileGenerator.myProjectScope)[0];
                    PsiClass model = contractClass.findInnerClassByName("Model", false);//don't need to search base
                    psiClass.getImplementsList().add(fileGenerator.myFactory.createClassReferenceElement(model));
                    psiClass.getModifierList().setModifierProperty("public", true);//force
                }
            });
            //生成Presenter文件
            fileGenerator.generateFile(myCurrentDir, className + "Presenter", JavaTemplateUtil.INTERNAL_CLASS_TEMPLATE_NAME, new FileGenerator.onFileGeneratedListener() {
                @Override
                public void onJavaFileGenerated(PsiJavaFile javaFile, PsiClass psiClass) {
                    PsiClass contractClass = fileGenerator.myShortNamesCache.getClassesByName(fileGenerator.myPrefix + "Contract", fileGenerator.myProjectScope)[0];
                    PsiClass model = contractClass.findInnerClassByName("Presenter", false);//don't need to search base
                    psiClass.getExtendsList().add(fileGenerator.myFactory.createClassReferenceElement(model));
                    psiClass.getModifierList().setModifierProperty("public", true);//force
                }
            });
        }
    }

    /**
     * 通过文件读写的方式
     */
    private void createFileWithContract() {
        ClassCreateHelper.createImplClass(e, this.getCurrentPath(this.e, this.classModel.getClassName()), this.packageName, 0, this.classModel);
        ClassCreateHelper.createImplClass(e, this.getCurrentPath(this.e, this.classModel.getClassName()), this.packageName, 1, this.classModel);
    }

    private String getCurrentPath(AnActionEvent e, String classFullName) {
        VirtualFile currentFile = (VirtualFile) DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        assert currentFile != null;
        String path = currentFile.getPath().replace(classFullName + ".java", "");
        return path;
    }

    /**
     * 编写代码
     */
    private void createContractContent() {
        int lastIndex = this.content.lastIndexOf("}");
        this.content = this.content.substring(0, lastIndex);
        MessagesCenter.showDebugMessage(this.content, "debug");
        final String content = this.getContractContent();
        WriteCommandAction.runWriteCommandAction(this.editor.getProject(), new Runnable() {
            public void run() {
                MVPDataBind.this.editor.getDocument().setText(content);
            }
        });
    }

    private String getContractContent() {
        return this.content.trim() + "\n\n    interface View extends BaseView {\n      \n    }\n\n    interface Model extends BaseModel {\n        \n    }\n\n    abstract class Presenter extends BasePresenter<Model, View> {\n        \n    }\n\n}";
    }

    /**
     * 检查文件可用性
     *
     * @return
     */
    private boolean checkCanUse() {
        this.content = this.editor.getDocument().getText();
        String[] words = this.content.split(" ");
        String[] var2 = words;
        int var3 = words.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            String word = var2[var4];
            if (word.contains("Contract")) {
                String className = word.substring(0, word.indexOf("Contract"));
                this.classModel.setFunctionName(className);
                this.classModel.setClassName(word);
                MessagesCenter.showDebugMessage(className, "class name");
            }
        }

        if (TextUtils.isEmpty(this.classModel.getFunctionName())) {
            Messages.showErrorDialog(
                    "文件名定义不规范",
                    "Error");
//            MessagesCenter.showErrorMessage("Create failed ,Can't found 'Contract'  in your class name,your class name must contain 'Contract'", "error");
            return false;
        } else {
            return true;
        }
    }
}
