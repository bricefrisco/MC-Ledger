package com.ledger.api.utils;
import java.util.*;

public class TimeBucketRetriever {
    // daily: every 10 minutes
    // weekly: every hour
    // monthly: every 4 hours
    // all-time: every 1 day

    public static List<TimeBucket> retrieveTimeBucketsAllTime(long lastRun) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(lastRun);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        List<TimeBucket> result = new ArrayList<>();
        long current = lastRun;
        long now = System.currentTimeMillis();
        while (current <= now) {
            long start = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            long end = cal.getTimeInMillis();

            TimeBucket timeBucket = new TimeBucket();
            timeBucket.setStart(start);
            timeBucket.setEnd(end);
            result.add(timeBucket);

            current = end;
        }

        return result;
    }

    public static List<TimeBucket> retrieveTimeBucketsMonthly(long lastRun) {
        if (lastRun == 0L) {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.add(Calendar.MONTH, -1);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            lastRun = c.getTimeInMillis();
        }

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(lastRun);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        List<TimeBucket> result = new ArrayList<>();
        long current = lastRun;
        long now = System.currentTimeMillis();
        while (current <= now) {
            long start = cal.getTimeInMillis();
            cal.add(Calendar.HOUR, 4);
            long end = cal.getTimeInMillis();

            TimeBucket timeBucket = new TimeBucket();
            timeBucket.setStart(start);
            timeBucket.setEnd(end);
            result.add(timeBucket);

            current = end;
        }

        return result;
    }

    public static List<TimeBucket> retrieveTimeBucketsWeekly(long lastRun) {
        if (lastRun == 0L) {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.add(Calendar.DAY_OF_YEAR, -7);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            lastRun = c.getTimeInMillis();
        }

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(lastRun);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        List<TimeBucket> result = new ArrayList<>();
        long current = lastRun;
        long now = System.currentTimeMillis();
        while (current <= now) {
            long start = cal.getTimeInMillis();
            cal.add(Calendar.HOUR, 1);
            long end = cal.getTimeInMillis();

            TimeBucket timeBucket = new TimeBucket();
            timeBucket.setStart(start);
            timeBucket.setEnd(end);
            result.add(timeBucket);

            current = end;
        }

        return result;
    }

    public static List<TimeBucket> retrieveTimeBucketsDaily(long lastRun) {
        if (lastRun == 0L) {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            lastRun = c.getTimeInMillis();
        }

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(lastRun);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        List<TimeBucket> result = new ArrayList<>();
        long current = lastRun;
        long now = System.currentTimeMillis();
        while (current <= now) {
            long start = cal.getTimeInMillis();
            cal.add(Calendar.MINUTE, 10);
            long end = cal.getTimeInMillis();

            TimeBucket timeBucket = new TimeBucket();
            timeBucket.setStart(start);
            timeBucket.setEnd(end);
            result.add(timeBucket);

            current = end;
        }

        return result;
    }

    public static class TimeBucket {
        private long start;
        private long end;

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public void setStart(long start) {
            this.start = start;
        }

        public void setEnd(long end) {
            this.end = end;
        }

        @Override
        public String toString() {
            return "TimeBucket{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }
}
