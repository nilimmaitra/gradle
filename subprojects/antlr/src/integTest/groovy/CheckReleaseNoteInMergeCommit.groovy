import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class CheckReleaseNoteInMergeCommit {
    private static final THREAD_POOL = Executors.newCachedThreadPool()

    private static final List<String> MONITORED_FILES = [
        "subprojects/docs/src/docs/release/notes.md"
    ]

    static void main(String[] commits) {
        try {
            commits.each { checkCommit(it) }
        } finally {
            THREAD_POOL.shutdown()
        }
    }

    static void checkCommit(String commit) {
        List<String> parentCommits = parentCommitsOf(commit)
        if (parentCommits.size() != 2) {
            println("$commit is not a merge commit we're looking for. Parents: $parentCommits")
        }

        // The correct state we are looking for is:
        // 1. It's a merge commit.
        // 2. The first parent commit is from master only.
        // 3. The second parent commit is from master and release branch.
        // Otherwise, skip this commit.
        List<String> p1Branches = branchesOf(parentCommits[0])
        if (!p1Branches.contains("master")) {
            println("The 1st parent commit ${parentCommits[0]} doesn't contain master ($p1Branches), skip.")
            return
        }

        List<String> p2Branches = branchesOf(parentCommits[1])
        if (!p2Branches.contains("master") || !p2Branches.any { it.startsWith("release") }) {
            println("The 2nd parent commit ${parentCommits[1]} doesn't contain master/releaseX ($p2Branches), skip.")
            return
        }

        List<String> badFiles = MONITORED_FILES.grep { isBadFileInMergeCommit(it, commit, parentCommits[0], parentCommits[1]) }
        throw new RuntimeException("Found bad files in merge commit $commit: $badFiles")
    }

    /**
     * Check if the given file is "bad": we should only use the release note from the master branch.
     * This means that every line in the merge commit version should be either:
     * - Only exists on `master`.
     * - Exists on `master` and `releaseX`.
     * If any line is only present on `releaseX` version, then it's a bad file.
     * Also, we ignore empty lines.
     */
    static boolean isBadFileInMergeCommit(String filePath, String mergeCommit, String masterCommit, String releaseCommit) {
        List<String> mergeCommitFileLines = showFileOnCommit(mergeCommit, filePath).readLines()
        List<String> masterCommitFileLines = showFileOnCommit(masterCommit, filePath).readLines()
        List<String> releaseCommitFileLines = showFileOnCommit(releaseCommit, filePath).readLines()
        for (String line in mergeCommitFileLines) {
            if (line.trim().isEmpty()) {
                continue
            }
            if (!masterCommitFileLines.contains(line) && releaseCommitFileLines.contains(line)) {
                println("Found bad file $filePath in merge commit $mergeCommit: '$line' only exists in $releaseCommit but not in $masterCommit")
                return true
            }
        }
        return false
    }

    static String showFileOnCommit(String commit, String filePath) {
        return getStdout("git show $commit:$filePath")
    }

    static List<String> branchesOf(String commit) {
        return getStdout("git branch --contains $commit")
            .readLines()
            .collect { it.replace("*", "") } // remove the * from the current branch, e.g. * master -> master
            .collect { it.trim() }
            .grep { !it.isEmpty() }
    }

    static List<String> parentCommitsOf(String commit) {
        return getStdout("git show --format=%P --no-patch $commit")
            .split(" ").collect { it.trim() }.grep { !it.isEmpty() }
    }

    static String getStdout(String command) {
        Process process = command.execute()
        def stdoutFuture = readStreamAsync(process.inputStream)
        def stderrFuture = readStreamAsync(process.errorStream)

        int returnCode = process.waitFor()
        String stdout = stdoutFuture.get()
        String stderr = stderrFuture.get()

        assert returnCode == 0: "$command failed with return code: $returnCode, stdout: $stdout stderr: $stderr"
        return stdout
    }

    static Future<String> readStreamAsync(InputStream inputStream) {
        return THREAD_POOL.submit({ inputStream.text } as Callable) as Future<String>
    }
}
