import { test, expect } from '@playwright/test';
import { RegisterPage } from '../pages/RegisterPage';
import { LoginPage } from '../pages/LoginPage';
import * as path from 'path';

let registerPage: RegisterPage;
let loginPage: LoginPage;

test.beforeEach(async ({ page }) => {
  registerPage = new RegisterPage(page);
  loginPage = new LoginPage(page);
  await registerPage.goto();
});

test.describe('Account Provisioning Validation Suite', () => {
  test('TC-VAL-03: Positive Account Registration & Dynamic Redirect', async ({ page }, testInfo) => {
    const timestamp = Date.now();
    const uniqueUser = `reg_user_${timestamp}`;
    const email = `${uniqueUser}@example.com`;
    const password = 'Password@123';

    // ── STEP 1: Initial Form State (Pristine, 100% settled) ──
    const step1Shot = path.join('reports', 'screenshots', `TC-VAL-03_Step1_Initial_Form_${timestamp}.png`);
    await page.screenshot({ path: step1Shot, fullPage: true });
    await testInfo.attach('Step 1: Pristine Registration Form State', {
      path: step1Shot,
      contentType: 'image/png'
    });

    // ── STEP 2: Input Population ──
    await registerPage.fillRegistrationForm(uniqueUser, email, password, 'ROLE_EMPLOYEE');
    const step2Shot = path.join('reports', 'screenshots', `TC-VAL-03_Step2_Populated_Data_${timestamp}.png`);
    await page.screenshot({ path: step2Shot, fullPage: true });
    await testInfo.attach('Step 2: Populated Account Data Prior to Submission', {
      path: step2Shot,
      contentType: 'image/png'
    });

    // ── STEP 3: Submission & Success Banner Verification ──
    await registerPage.submit();
    await registerPage.waitForSuccess();
    await expect(registerPage.successAlert).toContainText('Account created successfully');

    const step3Shot = path.join('reports', 'screenshots', `TC-VAL-03_Step3_Success_Banner_${timestamp}.png`);
    await page.screenshot({ path: step3Shot, fullPage: true });
    await testInfo.attach('Step 3: Account Creation Confirmation Banner Displayed', {
      path: step3Shot,
      contentType: 'image/png'
    });

    // ── STEP 4: Dynamic Navigation to Login (Guaranteed Settled, No Blank Screen) ──
    await page.waitForURL('**/login');
    // Senior SDET Pattern: Wait for the incoming LoginPage heading and card animations to fully mount and settle
    await loginPage.waitForLoaded();

    const step4Shot = path.join('reports', 'screenshots', `TC-VAL-03_Step4_Redirected_Login_${timestamp}.png`);
    await page.screenshot({ path: step4Shot, fullPage: true });
    await testInfo.attach('Step 4: Automatic Dynamic Redirect to Login Portal (Fully Rendered)', {
      path: step4Shot,
      contentType: 'image/png'
    });
  });

  test('TC-VAL-04: Negative Account Registration on Existing Username Collision', async ({ page }, testInfo) => {
    const timestamp = Date.now();
    const collisionUser = `collision_${timestamp}`;
    const email = `${collisionUser}@example.com`;
    const password = 'Password@123';

    // Precondition: Seed initial user so it exists in database
    await registerPage.register(collisionUser, email, password, 'ROLE_EMPLOYEE');
    await registerPage.waitForSuccess();

    // ── STEP 1: Initial State for Duplicate Attempt ──
    await registerPage.goto();
    const step1Shot = path.join('reports', 'screenshots', `TC-VAL-04_Step1_Initial_Form_${timestamp}.png`);
    await page.screenshot({ path: step1Shot, fullPage: true });
    await testInfo.attach('Step 1: Fresh Registration Form for Duplicate Submission', {
      path: step1Shot,
      contentType: 'image/png'
    });

    // ── STEP 2: Duplicate Input Entry ──
    await registerPage.fillRegistrationForm(collisionUser, `dup_${email}`, password, 'ROLE_EMPLOYEE');
    const step2Shot = path.join('reports', 'screenshots', `TC-VAL-04_Step2_Populated_Duplicate_${timestamp}.png`);
    await page.screenshot({ path: step2Shot, fullPage: true });
    await testInfo.attach('Step 2: Populated Existing Username Prior to Submission', {
      path: step2Shot,
      contentType: 'image/png'
    });

    // ── STEP 3: Collision Rejection Verification (Settled, Solid, Crisp) ──
    await registerPage.submit();
    await registerPage.waitForError();
    await expect(registerPage.errorAlert).toContainText('Username exists');

    const step3Shot = path.join('reports', 'screenshots', `TC-VAL-04_Step3_Collision_Error_${timestamp}.png`);
    await page.screenshot({ path: step3Shot, fullPage: true });
    await testInfo.attach('Step 3: Rejection Alert Verified (Username exists)', {
      path: step3Shot,
      contentType: 'image/png'
    });
  });
});
