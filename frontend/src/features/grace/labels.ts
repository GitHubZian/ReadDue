import type { GraceStatus } from "@/lib/api/models";
import type { Tone } from "@/lib/api/types";

export const graceStatusMeta: Record<GraceStatus, { label: string; tone: Tone }> = {
  PENDING: { label: "待裁决", tone: "amber" },
  APPROVED: { label: "已通过", tone: "mint" },
  REJECTED: { label: "已驳回", tone: "red" },
  EXPIRED: { label: "已失效", tone: "slate" },
};

/** 后端返回库内码 GRADUATION|EXAM|THESIS|NORMAL，未命中时原样展示。 */
export const graceSceneLabel = (scene: string) =>
  ({ GRADUATION: "毕业离校", EXAM: "集中考试", THESIS: "论文写作", NORMAL: "合理事由" })[
    scene
  ] ?? scene;
