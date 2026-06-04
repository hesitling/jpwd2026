#!/bin/bash
# 密码管理器启动脚本

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# 使用项目内的 JBR 和 Maven
export JAVA_HOME="$SCRIPT_DIR/jbr"
MVN="$SCRIPT_DIR/maven/lib/maven3/bin/mvn"

# 预检查 JBR 和 Maven 路径
if [ ! -d "$JAVA_HOME" ]; then
  echo "ERROR: Java runtime directory not found at $JAVA_HOME" >&2
  exit 1
fi

if [ ! -x "$JAVA_HOME/bin/java" ]; then
  echo "ERROR: Java executable not found at $JAVA_HOME/bin/java" >&2
  exit 1
fi

if [ ! -x "$MVN" ]; then
  echo "ERROR: Maven not found at $MVN" >&2
  exit 1
fi

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

$MVN package -q -DskipTests

# 获取依赖路径
CLASSPATH="target/classes"
for jar in target/dependency/*.jar; do
  [ -f "$jar" ] && CLASSPATH="$CLASSPATH:$jar"
done

# 如果没有 dependency 目录，手动复制依赖
if [ ! -d "target/dependency" ]; then
  $MVN dependency:copy-dependencies -q -DoutputDirectory=target/dependency
  CLASSPATH="target/classes"
  for jar in target/dependency/*.jar; do
    [ -f "$jar" ] && CLASSPATH="$CLASSPATH:$jar"
  done
fi

exec "$JAVA_HOME/bin/java" \
  -Xmx256m \
  -Dfile.encoding=UTF-8 \
  -cp "$CLASSPATH" \
  org.florious.passwordmanager.Main
