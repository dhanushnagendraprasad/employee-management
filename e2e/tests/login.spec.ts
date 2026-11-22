import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';
import * as path from 'path';

let loginPage: LoginPage;
let dashboardPage: DashboardPage;

test.beforeEach(async ({ page }) => {
  loginPage = new LoginPage(page);
  dashboardPage = new DashboardPage(page);
});

test.describe('Authentication Regression Validation Suite', () => {
  test('TC-VAL-01: Positive Authentication & Full Session Lifecycle', async ({ page }, testInfo) => {
    // 1. Arrange: Provision hermetic test user via API
    const timestamp = Date.now();
    const username = `val_user_${timestamp}`;
    const password = 'Password@123';

    await page.request.post('/api/auth/register', {
      data: {
        username,
        email: `${username}@example.com`,
        password,
        role: 'ROLE_ADMIN'
      }
    });

    // ── STEP 1: Form Navigation (Empty State) ──
    await loginPage.goto();
    const step1Shot = path.join('reports', 'screenshots', `TC-VAL-01_Step1_Initial_Form_${timestamp}.png`);
    await page.screenshot({ path: step1Shot, fullPage: true });
    await testInfo.attach('Step 1: Initial Pristine Login Form State', {
      path: step1Shot,
      contentType: 'image/png'
    });

    // ── STEP 2: Input Population ──
    await loginPage.enterUsername(username);
    await loginPage.enterPassword(password);
    const step2Shot = path.join('reports', 'screenshots', `TC-VAL-01_Step2_Populated_Form_${timestamp}.png`);
    await page.screenshot({ path: step2Shot, fullPage: true });
    await testInfo.attach('Step 2: Populated Credentials Prior to Submission', {
      path: step2Shot,
      contentType: 'image/png'
    });

    // ── STEP 3: Submission & Dynamic Wait for Data Render ──
    await loginPage.submit();
    await page.waitForURL('**/dashboard');
    // Dynamic wait: wait until loading spinner vanishes and table renders
    await dashboardPage.waitForDataLoaded();

    const step3Shot = path.join('reports', 'screenshots', `TC-VAL-01_Step3_Dashboard_Loaded_${timestamp}.png`);
    await page.screenshot({ path: step3Shot, fullPage: true });
    await testInfo.attach('Step 3: Authenticated State with Directory Data Loaded', {
      path: step3Shot,
      contentType: 'image/png'
    });

    // ── STEP 4: Session Teardown (Logout Verification) ──
    await dashboardPage.logout();
    await expect(page).toHaveURL(/.*login/);
    await expect(loginPage.submitButton).toBeVisible();

    const step4Shot = path.join('reports', 'screenshots', `TC-VAL-01_Step4_Logout_Proof_${timestamp}.png`);
    await page.screenshot({ path: step4Shot, fullPage: true });
    await testInfo.attach('Step 4: Clean Session Termination & Logout Confirmation Proof', {
      path: step4Shot,
      contentType: 'image/png'
    });
  });

  test('TC-VAL-02: Negative Authentication with Invalid Password', async ({ page }, testInfo) => {
    const timestamp = Date.now();

    // ── STEP 1: Empty Form State ──
    await loginPage.goto();
    const step1Shot = path.join('reports', 'screenshots', `TC-VAL-02_Step1_Initial_Form_${timestamp}.png`);
    await page.screenshot({ path: step1Shot, fullPage: true });
    await testInfo.attach('Step 1: Initial Login Form State', {
      path: step1Shot,
      contentType: 'image/png'
    });

    // ── STEP 2: Invalid Data Entry ──
    await loginPage.enterUsername('non_existent_operator');
    await loginPage.enterPassword('InvalidPassword!999');
    const step2Shot = path.join('reports', 'screenshots', `TC-VAL-02_Step2_Populated_Invalid_${timestamp}.png`);
    await page.screenshot({ path: step2Shot, fullPage: true });
    await testInfo.attach('Step 2: Populated Invalid Credentials Prior to Submission', {
      path: step2Shot,
      contentType: 'image/png'
    });

    // ── STEP 3: Server Rejection Verification ──
    await loginPage.submit();
    await loginPage.waitForError();
    await expect(loginPage.errorAlert).toContainText('Bad credentials');

    const step3Shot = path.join('reports', 'screenshots', `TC-VAL-02_Step3_Error_Banner_${timestamp}.png`);
    await page.screenshot({ path: step3Shot, fullPage: true });
    await testInfo.attach('Step 3: Authentication Rejection Banner Displayed (Bad credentials)', {
      path: step3Shot,
      contentType: 'image/png'
    });
  });
});
