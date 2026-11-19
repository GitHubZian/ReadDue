package com.zian.backend.notify.application;

/**
 * 通知文案统一出口。
 *
 * <p>站内信列表与邮件正文共用同一套措辞，避免两处各写一份后逐渐漂移。
 */
final class NotifyCopy {

    private NotifyCopy() {
    }

    /**
     * 通知标题。
     *
     * @param level 通知级别
     * @return 中文标题
     */
    static String title(String level) {
        return switch (level == null ? "" : level) {
            case NotifyService.LEVEL_L1 -> "图书到期前提醒";
            case NotifyService.LEVEL_L2 -> "图书今日到期";
            case NotifyService.LEVEL_L3 -> "图书逾期提醒";
            case NotifyService.LEVEL_GRACE -> "宽限申请结果";
            case NotifyService.LEVEL_READY -> "预约到书通知";
            case NotifyService.LEVEL_APPEAL -> "申诉受理通知";
            default -> "阅有期通知";
        };
    }

    /**
     * 通知正文。
     *
     * @param level       通知级别
     * @param orderId     借阅单 ID，可为空
     * @param bookTitle   书目名称，可为空
     * @param overdueDays 逾期天数，可为空
     * @return 中文正文
     */
    static String body(String level, Long orderId, String bookTitle, Integer overdueDays) {
        String book = bookTitle == null ? "您借阅的图书" : "《" + bookTitle + "》";
        String text = switch (level == null ? "" : level) {
            case NotifyService.LEVEL_L1 -> book + "即将到期，请按时归还。";
            case NotifyService.LEVEL_L2 -> book + "今日到期，请按时归还。";
            case NotifyService.LEVEL_L3 -> overdueDays != null && overdueDays > 0
                    ? book + "已逾期 " + overdueDays + " 天，请尽快归还。"
                    : book + "已逾期，请尽快归还。";
            case NotifyService.LEVEL_GRACE -> "您的宽限申请已有处理结果，请查看详情。";
            case NotifyService.LEVEL_APPEAL -> "您的申诉已受理，我们会尽快处理。";
            case NotifyService.LEVEL_READY -> "您预约的图书已到馆，请及时取书。";
            default -> "您有一条新的系统通知。";
        };
        return orderId == null ? text : text + "借阅单：" + orderId + "。";
    }
}
