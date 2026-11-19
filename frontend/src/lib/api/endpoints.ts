/** 后端 context-path 为 /api，业务前缀 /v1，dev 由 Vite 代理到 8125。 */
export const endpoints = {
  auth: {
    login: "/auth/login",
    me: "/me",
    logout: "/auth/logout",
  },
  system: {
    health: "/system/health",
  },
  dashboard: "/admin/dashboard",
  graces: {
    list: "/admin/graces",
    decide: (id: string) => `/admin/graces/${id}/approve`,
    reject: (id: string) => `/admin/graces/${id}/reject`,
  },
  appeals: {
    list: "/admin/appeals",
    ack: (id: string) => `/admin/appeals/${id}/ack`,
    resolve: (id: string) => `/admin/appeals/${id}/resolve`,
    reject: (id: string) => `/admin/appeals/${id}/reject`,
    compensate: (id: string) => `/admin/appeals/${id}/compensate`,
  },
  reservations: {
    list: "/admin/reservations",
    promote: (id: string) => `/admin/reservations/${id}/promote`,
    expire: (id: string) => `/admin/reservations/${id}/expire`,
  },
  overdue: {
    list: "/admin/overdue",
    remind: "/admin/remind/batch",
  },
  notify: {
    templates: "/admin/templates",
    tasks: "/admin/notify-tasks",
    retry: (id: string) => `/admin/notify-tasks/${id}/retry`,
    topics: "/admin/notify-topics",
    topic: (code: string) => `/admin/notify-topics/${code}`,
  },
  users: {
    list: "/admin/users",
    blacklist: (id: string) => `/admin/users/${id}/blacklist`,
    restore: (id: string) => `/admin/users/${id}/restore`,
  },
  rules: {
    root: "/admin/rules",
    tiers: "/admin/tiers",
    calendar: "/admin/calendar",
    calendarItem: (id: string) => `/admin/calendar/${id}`,
  },
  roles: {
    list: "/admin/roles",
    permissions: "/admin/permissions",
    permissionsOf: (code: string) => `/admin/roles/${code}/permissions`,
  },
  files: {
    upload: "/files",
    download: (id: string) => `/files/${id}`,
  },
  audit: {
    list: "/admin/audit-logs",
    detail: (id: string) => `/admin/audit-logs/${id}`,
    export: "/admin/audit-logs/export",
  },
  circulation: {
    borrow: "/borrows",
    return: "/returns",
    today: "/admin/circulation/today",
  },
} as const;
