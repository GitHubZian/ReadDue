package com.zian.backend.iam.domain.repository;

/**
 * 「我的」资料只读查询：读者档案字段与累计借阅数。
 *
 * <p>累计借阅横跨 {@code borrow_order}，此处只做计数、不写借阅状态；调用方必须传当前登录用户，
 * 不允许从请求参数接收用户 ID。
 */
public interface ReaderSelfRepository {

    /**
     * 读取读者的院系/专业/年级；无档案行时三个字段均为空字符串。
     *
     * @param userId 读者用户 ID
     * @return 档案字段
     */
    Profile profile(long userId);

    /**
     * 全部借阅单数（含已归还、逾期与丢失）。
     *
     * @param userId 读者用户 ID
     * @return 累计单数，无记录为 0
     */
    long totalBorrowed(long userId);

    /**
     * 读者档案字段；数据库 NULL 统一折叠为空字符串，便于客户端直接展示。
     *
     * @param department 院系
     * @param major      专业
     * @param grade      年级
     */
    record Profile(String department, String major, String grade) {

        /** 无档案行时的默认值。 */
        public static Profile empty() {
            return new Profile("", "", "");
        }
    }
}
