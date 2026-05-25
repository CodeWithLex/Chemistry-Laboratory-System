# Resend API Migration Guide

To ensure reliable email notifications on Render's free tier, we have switched from Gmail SMTP to the **Resend API**.

## 1. Get an API Key

1. Sign up at [resend.com](https://resend.com).
2. Create an API Key with 'Full Access'.
3. Copy the key (starts with `re_`).

## 2. Configure Render

Go to your Render Dashboard → Select your service → **Environment** and add:

- `RESEND_API_KEY`: Paste your key here.
- `ADMIN_EMAIL`: Your destination email (use the one you registered with Resend if on the free tier).
- `MAIL_FROM_NAME`: ChemLab System

## 3. Verified Domain (Optional)

On the Resend free tier, you are limited to sending to your own registered email address. To send to any student/faculty email, you must:

1. Go to **Domains** in Resend.
2. Add your school domain.
3. Update `server.js` (line 150) to use your verified domain instead of `@resend.dev`.
