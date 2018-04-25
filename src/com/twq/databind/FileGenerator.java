package com.twq.databind;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;

/**
 * 创建文件
 */
public class FileGenerator {

    protected Project myProject;//current java project
    protected PsiDirectory myContractDir;//the contract package dir
    protected PsiDirectory myModelDir;//the model package dir
    protected PsiDirectory myPresenterDir;//the presenter package dir
    protected String myPrefix;//the prefix used to identify each other
    protected PsiElementFactory myFactory;//the factory used to generate interface/class/innerClass/classReference
    protected JavaDirectoryService myDirectoryService;//the dirService used to generate files under particular dir(package)
    protected PsiShortNamesCache myShortNamesCache;//used to search a class in particular scope
    protected GlobalSearchScope myProjectScope;//just this project is enough

    FileGenerator(Project project, String prefix) {
        this.myProject = project;
        this.myPrefix = prefix;
        myShortNamesCache = PsiShortNamesCache.getInstance(project);
        myFactory = JavaPsiFacade.getElementFactory(project);
        myDirectoryService = JavaDirectoryService.getInstance();
        myProjectScope = GlobalSearchScope.projectScope(project);
    }

    FileGenerator(Project project, PsiDirectory contractDir, PsiDirectory modelDir, PsiDirectory presenterDir, String prefix) {
        this.myProject = project;
        this.myContractDir = contractDir;
        this.myModelDir = modelDir;
        this.myPresenterDir = presenterDir;
        this.myPrefix = prefix;
        myShortNamesCache = PsiShortNamesCache.getInstance(project);
        myFactory = JavaPsiFacade.getElementFactory(project);
        myDirectoryService = JavaDirectoryService.getInstance();
        myProjectScope = GlobalSearchScope.projectScope(project);
    }

    public void generateFile(final PsiDirectory directory, final String fileName, final String type, final onFileGeneratedListener listener) {
        WriteCommandAction.runWriteCommandAction(myProject, new Runnable() {
            @Override
            public void run() {
                PsiClass[] psiClasses = myShortNamesCache.getClassesByName(fileName, myProjectScope);//NotNull
                PsiClass psiClass;
                PsiJavaFile javaFile;
                if (psiClasses.length != 0) {//if the class already exist.
                    psiClass = psiClasses[0];
                    javaFile = (PsiJavaFile) psiClass.getContainingFile();
                    javaFile.delete();//then delete the old one
                }//and re-generate one
                psiClass = myDirectoryService.createClass(directory, fileName, type);
                javaFile = (PsiJavaFile) psiClass.getContainingFile();
                PsiPackage psiPackage = myDirectoryService.getPackage(directory);
                javaFile.setPackageName(psiPackage.getQualifiedName());
                listener.onJavaFileGenerated(javaFile, psiClass);
            }
        });

    }

    public interface onFileGeneratedListener {
        /**
         * When the file has been generated, then the listener will be called.
         *
         * @param javaFile the PsiJavaFile generated just now
         * @param psiClass the corresponding PsiClass
         */
        void onJavaFileGenerated(PsiJavaFile javaFile, PsiClass psiClass);
    }
}
