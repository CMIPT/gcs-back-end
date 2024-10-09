PIDDIR=$(dirname "$PIDFILE")
LOGDIR=$(dirname "$LOGFILE")
start() {
  if [ -f "$PIDFILE" ] && kill -0 "$(cat "$PIDFILE")"; then
    echo 'Service already running' >&2
    return 1
  fi
  echo 'Starting service…' >&2
  su -c "mkdir -p ""$PIDDIR" "$RUNAS"
  su -c "mkdir -p ""$LOGDIR" "$RUNAS"
  su -c "$SCRIPT & echo \$!" "$RUNAS" > "$LOGFILE"
  head -1 "$LOGFILE" > "$PIDFILE"
  echo 'Service started' >&2
}

stop() {
  if [ ! -f "$PIDFILE" ] || ! kill -0 "$(cat "$PIDFILE")"; then
    echo 'Service not running' >&2
    return 1
  fi
  echo 'Stopping service…' >&2
  kill -15 "$(cat "$PIDFILE")" && rm -f "$PIDFILE"
  echo 'Service stopped' >&2
}

uninstall() {
  stop
  rm -f "$PIDFILE"
  echo "Notice: log file is not be removed: '$LOGFILE'" >&2
  update-rc.d -f "$NAME" remove
  rm -fv "$0"
}

case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  uninstall)
    uninstall
    ;;
  restart)
    stop
    start
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|uninstall}"
esac
