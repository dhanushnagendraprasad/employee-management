import { test, expect } from '@playwright/test';

test.describe('Authentication Flows', () => {
  test('should allow a new user to sign up and login', async ({ page }) => {
    const timestamp = Date.now();
    const username = `testuser_${timestamp}`;
    const email = `${username}@example.com`;
    const password = 'password123';

    // Signup
    await page.goto('/signup');
    await page.fill('input[placeholder="Username"]', username);
    await page.fill('input[placeholder="Email"]', email);
    await page.fill('input[placeholder="Password"]', password);
    await page.click('button[type="submit"]');

    // Wait for redirect to login or dashboard
    await page.waitForURL('**/dashboard');
    await expect(page.getByText('Employee Directory')).toBeVisible();
    await expect(page.getByText(username)).toBeVisible();
  });

  test('should allow an admin to login and see employee grid', async ({ page }) => {
    // We assume an admin user exists or the backend creates one by default
    await page.goto('/login');
    // Using a fallback for testing purposes
    await page.fill('input[placeholder="Username"]', 'admin');
    await page.fill('input[placeholder="Password"]', 'admin');
    await page.click('button[type="submit"]');

    // Dashboard
    await page.waitForURL('**/dashboard');
    await expect(page.locator('.employee-grid')).toBeVisible();
    
    // Check if Add Employee button is visible for admin
    await expect(page.getByText('Add Employee')).toBeVisible();
  });
});
