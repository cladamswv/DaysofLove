# Upgrade the existing Days of Love GitHub repo to v2

Upload `DaysOfLove-v2-github-ready.zip` to the root of your existing GitHub repository, then open the repository in Codespaces and paste this command into the terminal:

```bash
rm -rf /tmp/dol-v2 && mkdir -p /tmp/dol-v2 && unzip -q DaysOfLove-v2-github-ready.zip -d /tmp/dol-v2 && rsync -a --delete --exclude .git /tmp/dol-v2/DaysOfLove-v2/ ./ && git add -A && git commit -m "Days of Love v2 romantic upgrade" && git push
```

The push automatically starts **Build Android APK**.

When the build finishes:

1. Open the green workflow run.
2. Download the **DaysOfLove-v2-APK** artifact.
3. Extract the ZIP.
4. Install `app-debug.apk`.

## If Android says the app cannot be installed

The original v1 was signed by an ephemeral GitHub debug key. v2 now uses a stable test key, so Android may see the signatures as different. Uninstall v1, then install v2 and enter your partner/date again.

Future beta builds using this v2 project will keep the same test signature and should install as normal updates.
