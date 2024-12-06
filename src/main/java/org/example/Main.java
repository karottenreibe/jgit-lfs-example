package org.example;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lfs.LfsBlobFilter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        String repoPath = "/home/k/proj/jgit/repo/lfs.git"; // Path to your .git directory
        String filePath = "lfs.java"; // Path to the file in the repository

        // Open the repository
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File(repoPath))
                .readEnvironment()
                .findGitDir()
                .build();

        // Open the Git object
        try (Git git = new Git(repository)) {
            // Get the HEAD commit
            ObjectId head = repository.resolve("HEAD");
            System.out.println("HEAD = " + head);

            ObjectId treeId = repository.parseCommit(head).getTree().getId();

            // Create a TreeWalk to iterate over the files
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(treeId);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(filePath));

                if (treeWalk.next()) {
                    // Get the file content
                    ObjectLoader loader = repository.open(treeWalk.getObjectId(0));
                    // this line loads the file from LFS instead of the repo
                    loader = LfsBlobFilter.smudgeLfsBlob(repository, loader);
                    byte[] fileContent = loader.getBytes();
                    String content = new String(fileContent);
                    System.out.println("File Content:\n" + content);
                } else {
                    System.out.println("File not found in the repository.");
                }
            }
        }
    }
}
