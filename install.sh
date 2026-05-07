#!/bin/bash
# =============================================================================
#  cHIMS Unified Installation / Update Script
#  Supports: Ubuntu 20.04+ LTS
#
#  First run on a fresh server  → installs OS packages, MySQL, Payara, JDBC
#                                  pool, builds the WAR and deploys it.
#  Subsequent runs (existing
#  Payara + cHIMS detected)     → automatically switches to UPDATE MODE:
#                                  rebuilds the WAR and redeploys it onto the
#                                  running domain. No prompts, no setup.
# =============================================================================

set -euo pipefail

# Colours
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; BOLD='\033[1m'; NC='\033[0m'
info()    { echo -e "${CYAN}[INFO]${NC}  $*"; }
success() { echo -e "${GREEN}[OK]${NC}    $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*" >&2; exit 1; }
section() { echo -e "\n${BOLD}--- $* ---${NC}"; }

# Config
NEW_VERSION="${NEW_VERSION:-0.2}"
APP_NAME="chims-${NEW_VERSION}"
WAR_FILE="target/chims-${NEW_VERSION}.war"
PAYARA_VERSION="5.2022.5"
PAYARA_HOME="${PAYARA_HOME:-$HOME/payara5}"
ASADMIN="${PAYARA_HOME}/bin/asadmin"
DB_NAME="chims_v2"
JNDI_NAME="jdbc/chims_v2"
POOL_NAME="chims_v2"

# Sanity checks
if [[ "$EUID" -eq 0 ]]; then error "Run as a normal user (with sudo access), not root."; fi
[[ -f "pom.xml" ]] || error "pom.xml not found. Run this script from the project root."

echo -e "${BOLD}cHIMS v${NEW_VERSION} Installer${NC}"

# Detect existing install → update mode
UPDATE_MODE=false
if [[ -x "${ASADMIN}" ]] && "${ASADMIN}" list-jdbc-connection-pools 2>/dev/null | grep -q "^${POOL_NAME}$"; then
    UPDATE_MODE=true
    info "Existing cHIMS installation detected — running in UPDATE MODE (rebuild + redeploy only)."
fi

if [[ "${UPDATE_MODE}" == "false" ]]; then
    # 1. System Packages
    section "1. Installing System Packages"
    sudo apt-get update -qq
    sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -qq \
      openjdk-11-jdk maven unzip git mysql-server wget curl gh

    # 2. MySQL Setup
    section "2. Configuring MySQL"
    sudo systemctl enable --now mysql
    read -rsp "Enter MySQL root password: " MYSQL_ROOT_PASS; echo
    read -rsp "Enter chimsuser password: " MYSQL_CHIMS_PASS; echo

    sudo mysql -u root <<MYSQL_SETUP
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASS}';
CREATE USER IF NOT EXISTS 'chimsuser'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_CHIMS_PASS}';
CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\`;
GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO 'chimsuser'@'localhost';
FLUSH PRIVILEGES;
MYSQL_SETUP

    # 3. Payara Setup
    section "3. Configuring Payara"
    if [[ ! -d "${PAYARA_HOME}" ]]; then
        info "Downloading and extracting Payara..."
        wget -q "https://repo1.maven.org/maven2/fish/payara/distributions/payara/${PAYARA_VERSION}/payara-${PAYARA_VERSION}.zip" -O /tmp/payara.zip
        unzip -q /tmp/payara.zip -d "$HOME"
        rm /tmp/payara.zip
    fi

    # MySQL Connector
    if [[ ! -f "${PAYARA_HOME}/glassfish/lib/mysql-connector-j-8.0.33.jar" ]]; then
        wget -q "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar" -P "${PAYARA_HOME}/glassfish/lib/"
    fi

    "${ASADMIN}" start-domain || true
    "${ASADMIN}" add-library "${PAYARA_HOME}/glassfish/lib/mysql-connector-j-8.0.33.jar" || true

    # JDBC Pool & Resource
    "${ASADMIN}" create-jdbc-connection-pool \
      --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \
      --restype javax.sql.DataSource \
      --property "ServerName=localhost:PortNumber=3306:DatabaseName=${DB_NAME}:User=chimsuser:Password=${MYSQL_CHIMS_PASS}:UseSSL=false:allowPublicKeyRetrieval=true:URL=jdbc\:mysql\://localhost\:3306/${DB_NAME}" \
      "${POOL_NAME}" || true

    "${ASADMIN}" create-jdbc-resource --connectionpoolid "${POOL_NAME}" "${JNDI_NAME}" || true
else
    # Update mode: just make sure domain is up before we redeploy
    if ! "${ASADMIN}" list-domains 2>/dev/null | grep -q "domain1 running"; then
        info "Starting domain1..."
        "${ASADMIN}" start-domain
    fi
fi

# 4. Build and Deploy / Redeploy (always runs)
section "4. Building and Deploying"
mvn clean package -DskipTests
[[ -f "${WAR_FILE}" ]] || error "Build failed: ${WAR_FILE} not found."

if "${ASADMIN}" list-applications 2>/dev/null | grep -q "^${APP_NAME} "; then
    info "${APP_NAME} already deployed — redeploying."
    "${ASADMIN}" redeploy --name="${APP_NAME}" "${WAR_FILE}"
else
    info "First-time deploy of ${APP_NAME}."
    "${ASADMIN}" deploy --force=true "${WAR_FILE}"
fi

success "Done."
SERVER_IP=$(hostname -I 2>/dev/null | awk '{print $1}')
info "Access at: http://${SERVER_IP:-localhost}:8080/${APP_NAME}"
