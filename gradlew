#!/bin/sh

#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
app_path="$0"

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}
    [ -n "$APP_HOME" ] && APP_HOME=$(cd "$APP_HOME" && pwd) || break
    app_path="${app_path%"${app_path##*/}"}../"
do
    :
done


APP_HOME=$(cd "${APP_HOME:-./}" && pwd -P) || exit

APP_NAME="Gradle"
APP_BASE_NAME=${0##*/}
export CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn() {
    echo "$*" >&2
}

die() {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
darwin=false
msys=false
cygwin=false
nix=false
case "$(uname)" in
Darwin*)
    darwin=true
    ;;
MSYS* | MINGW*)
    msys=true
    ;;
CYGWIN*)
    cygwin=true
    ;;
NWIN*)
    nix=true
    ;;
esac

# For Cygwin or MSYS, switch paths to Windows-native format before running java
if [ "$cygwin" = "true" ] || [ "$msys" = "true" ]; then
    APP_HOME=$(cygpath --path --mixed "$APP_HOME")
    APP_BASE_NAME=$(cygpath --path --mixed "$APP_BASE_NAME")
    CLASSPATH=$(cygpath --path --mixed "$CLASSPATH")
    JAVACMD=$(cygpath --path --mixed "$JAVACMD")

    # We build the pattern for arguments to be converted via cygpath
    ROOTDIRSRAW=$(find -L / -maxdepth 3 -type d -name sources 2> /dev/null | head -n 1)
    if [ -n "$ROOTDIRSRAW" ]; then
        ROOTDIRS=$(echo "$ROOTDIRSRAW" | awk 'BEGIN { FS="/" } END { for (i=2; i<NF; i++) printf "%s/", $i; if (NF>1) print $NF; else print "."; }')
        ROOTDIRS=$(echo "$ROOTDIRS" | sed 's|/|\\\\|g')
    fi
    [ -z "$ROOTDIRS" ] && ROOTDIRS=$(cygpath --path --windows /)
    TEMP_VAR=$(echo "$1" | sed -e 's|^/||' -e 's|/$||')
    TEST_ROOTS=$(cygpath --path --windows "/$TEMP_VAR")
    CLASSPATH=$(echo $CLASSPATH | sed -e 's|G:|cygpath --path --windows "G:"|g')
    # add a "..." to the end of each path component
    CLASSPATH=$(echo $CLASSPATH | sed 's/:$//')
fi

# JVM_OPTS is not correct here, use GRADLE_OPTS (the dcs meant that)
if [ -n "$GRADLE_OPTS" ]; then
    JVM_OPTS="$JVM_OPTS $GRADLE_OPTS"
fi

# Collect all arguments for the java command, following the shell keyword conventions
set -- \
        "-Dorg.gradle.appname=$APP_BASE_NAME" \
        "-Dorg.gradle.home=$APP_HOME" \
        "-classpath" "$CLASSPATH" \
        org.gradle.wrapper.GradleWrapperMain \
        "$@"

# by default we should be in the correct project dir, but when run from Homebrew we might be in the cellar.
if [ "$(pwd)" = "/" ]; then
    cd "$(dirname "$0")"
fi

exec "$JAVACMD" "$@"
