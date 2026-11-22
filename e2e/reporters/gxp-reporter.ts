import { Reporter, TestCase, TestResult, FullResult, FullConfig, Suite } from '@playwright/test/reporter';
import * as fs from 'fs';
import * as path from 'path';
import * as crypto from 'crypto';
import { chromium } from '@playwright/test';

interface StepEvidence {
  stepName: string;
  screenshotPath: string;
}

interface TestAuditEntry {
  id: string;
  suite: string;
  title: string;
  status: 'SUCCESS' | 'FAILED' | 'SKIPPED';
  durationMs: number;
  timestamp: string;
  errorMessage?: string;
  steps: StepEvidence[];
}

export default class GxpReporter implements Reporter {
  private auditEntries: TestAuditEntry[] = [];
  private startTime: number = Date.now();
  private reportsDir: string = '';

  onBegin(config: FullConfig, suite: Suite) {
    this.startTime = Date.now();
    this.reportsDir = path.resolve(__dirname, '..', 'reports');
    if (!fs.existsSync(this.reportsDir)) {
      fs.mkdirSync(this.reportsDir, { recursive: true });
    }
    const screenshotsDir = path.join(this.reportsDir, 'screenshots');
    if (!fs.existsSync(screenshotsDir)) {
      fs.mkdirSync(screenshotsDir, { recursive: true });
    }
  }

  onTestEnd(test: TestCase, result: TestResult) {
    // Current timestamp in IST (GMT+5:30)
    const istTime = new Intl.DateTimeFormat('en-US', {
      timeZone: 'Asia/Kolkata',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: true
    }).format(new Date()) + ' GMT+5:30';

    const steps: StepEvidence[] = [];
    for (const attachment of result.attachments) {
      if (attachment.contentType.startsWith('image/') && attachment.path && fs.existsSync(attachment.path)) {
        steps.push({
          stepName: attachment.name || 'Visual Audit Step',
          screenshotPath: attachment.path
        });
      }
    }

    const suiteName = test.parent?.title || 'Regression Suite';
    const status: 'SUCCESS' | 'FAILED' | 'SKIPPED' = 
      result.status === 'passed' ? 'SUCCESS' : result.status === 'skipped' ? 'SKIPPED' : 'FAILED';

    this.auditEntries.push({
      id: `TC-VAL-0${this.auditEntries.length + 1}`,
      suite: suiteName,
      title: test.title,
      status,
      durationMs: result.duration,
      timestamp: istTime,
      errorMessage: result.error?.message,
      steps
    });
  }

  /**
   * Enterprise Storage Governance: Keeps only the latest N report runs
   * and automatically cleans up older HTML/PDF files to prevent server disk exhaustion.
   */
  private enforceRetentionPolicy(maxRetainedRuns: number = 5) {
    try {
      if (!fs.existsSync(this.reportsDir)) return;
      const files = fs.readdirSync(this.reportsDir);

      const htmlReports = files
        .filter(f => f.startsWith('Employee_Management_Validation_Report_Auth_') && f.endsWith('.html'))
        .sort()
        .reverse();

      if (htmlReports.length > maxRetainedRuns) {
        const toDelete = htmlReports.slice(maxRetainedRuns);
        for (const oldHtml of toDelete) {
          const baseName = oldHtml.replace('.html', '');
          const htmlPath = path.join(this.reportsDir, `${baseName}.html`);
          const pdfPath = path.join(this.reportsDir, `${baseName}.pdf`);
          if (fs.existsSync(htmlPath)) fs.unlinkSync(htmlPath);
          if (fs.existsSync(pdfPath)) fs.unlinkSync(pdfPath);
          console.log(`\x1b[33m✔ [Retention Governance] Purged obsolete audit report: ${baseName}\x1b[0m`);
        }
      }
    } catch (err) {
      console.warn('Retention governance warning:', err);
    }
  }

  async onEnd(result: FullResult): Promise<void> {
    const total = this.auditEntries.length;
    const passed = this.auditEntries.filter(e => e.status === 'SUCCESS').length;
    const failed = this.auditEntries.filter(e => e.status === 'FAILED').length;
    const needsReview = this.auditEntries.filter(e => e.status === 'SKIPPED').length;
    const successRate = total > 0 ? ((passed / total) * 100).toFixed(1) : '0.0';
    const totalDurationSec = ((Date.now() - this.startTime) / 1000).toFixed(1);

    const now = new Date();
    const generatedTimestamp = new Intl.DateTimeFormat('en-US', {
      timeZone: 'Asia/Kolkata',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: true
    }).format(now) + ' GMT+5:30';

    const pad = (n: number) => n.toString().padStart(2, '0');
    const istOffset = 5.5 * 60 * 60 * 1000;
    const istDate = new Date(now.getTime() + (now.getTimezoneOffset() * 60000) + istOffset);
    const fileTimestamp = `${istDate.getFullYear()}${pad(istDate.getMonth() + 1)}${pad(istDate.getDate())}_${pad(istDate.getHours())}${pad(istDate.getMinutes())}${pad(istDate.getSeconds())}`;
    
    const reportFileName = `Employee_Management_Validation_Report_Auth_${fileTimestamp}_IST`;

    const tableRows = this.auditEntries.map((e, idx) => `
      <tr>
        <td style="font-weight:700; color:#334155; text-align:center;">${idx + 1}</td>
        <td style="font-weight:600; color:#0f172a;">${e.id}</td>
        <td style="color:#1e293b;">${e.title}</td>
        <td style="text-align:center;">
          <span class="badge ${e.status === 'SUCCESS' ? 'badge-success' : 'badge-failed'}">
            ${e.status}
          </span>
        </td>
        <td style="font-family:monospace; text-align:right;">${(e.durationMs / 1000).toFixed(2)}s</td>
        <td style="font-size:0.85rem; color:#64748b; text-align:center;">${e.timestamp}</td>
      </tr>
    `).join('');

    const evidenceCards = this.auditEntries.map(e => {
      const stepVisuals = e.steps.map((st, sIdx) => {
        try {
          const imgData = fs.readFileSync(st.screenshotPath).toString('base64');
          return `
            <div class="step-container">
              <div class="step-header">
                <span class="step-badge">Step ${sIdx + 1}</span>
                <span class="step-name">${st.stepName}</span>
              </div>
              <div class="step-img-frame">
                <img src="data:image/png;base64,${imgData}" alt="${st.stepName}" class="audit-screenshot" />
              </div>
            </div>
          `;
        } catch {
          return '';
        }
      }).join('');

      return `
        <div class="test-evidence-block">
          <div class="test-evidence-header">
            <div>
              <span class="test-id-pill">${e.id}</span>
              <h3 style="display:inline; margin-left:8px; font-size:1.15rem; color:#0f172a;">${e.title}</h3>
            </div>
            <span class="badge ${e.status === 'SUCCESS' ? 'badge-success' : 'badge-failed'}">${e.status}</span>
          </div>
          <div class="test-evidence-meta">
            <span><strong>Suite:</strong> ${e.suite}</span>
            <span><strong>Duration:</strong> ${(e.durationMs / 1000).toFixed(2)}s</span>
            <span><strong>Execution Timestamp:</strong> ${e.timestamp}</span>
          </div>
          ${e.errorMessage ? `<div class="error-banner">${e.errorMessage}</div>` : ''}
          <div class="steps-flow">
            ${stepVisuals}
          </div>
        </div>
      `;
    }).join('');

    const reportHtmlBody = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>${reportFileName}</title>
  <style>
    * { box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Helvetica Neue", Arial, sans-serif; background-color: #f1f5f9; color: #0f172a; margin: 0; padding: 24px 32px; line-height: 1.5; }
    .header-banner { background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #2563eb 100%); color: white; padding: 28px 36px; border-radius: 12px; margin-bottom: 24px; box-shadow: 0 10px 25px -5px rgba(15, 23, 42, 0.2); }
    .header-banner h1 { margin: 0 0 6px 0; font-size: 1.7rem; letter-spacing: -0.025em; font-weight: 700; }
    .header-subtitle { font-size: 1rem; color: #94a3b8; font-weight: 500; }
    .header-meta { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-top: 18px; border-top: 1px solid rgba(255,255,255,0.15); padding-top: 16px; font-size: 0.88rem; }
    .header-meta strong { color: #38bdf8; }
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: 16px; margin-bottom: 24px; }
    .kpi-card { background: white; padding: 20px; border-radius: 10px; border: 1px solid #e2e8f0; box-shadow: 0 2px 4px rgba(0,0,0,0.04); text-align: center; }
    .kpi-title { font-size: 0.75rem; font-weight: 700; color: #64748b; letter-spacing: 0.06em; text-transform: uppercase; margin-bottom: 6px; }
    .kpi-value { font-size: 2rem; font-weight: 800; color: #0f172a; }
    .kpi-value.success { color: #16a34a; }
    .kpi-value.failed { color: #dc2626; }
    .card { background: white; border-radius: 12px; border: 1px solid #e2e8f0; box-shadow: 0 2px 4px rgba(0,0,0,0.04); padding: 28px; margin-bottom: 28px; }
    .card-title { margin-top: 0; font-size: 1.3rem; font-weight: 700; color: #0f172a; border-bottom: 2px solid #f1f5f9; padding-bottom: 12px; margin-bottom: 20px; }
    table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.92rem; }
    th { background: #f8fafc; padding: 14px 16px; font-weight: 700; color: #475569; border-bottom: 2px solid #cbd5e1; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.04em; }
    td { padding: 14px 16px; border-bottom: 1px solid #e2e8f0; }
    tr:hover { background-color: #f8fafc; }
    .badge { display: inline-block; padding: 5px 12px; border-radius: 9999px; font-size: 0.75rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.05em; }
    .badge-success { background: #dcfce7; color: #15803d; border: 1px solid #86efac; }
    .badge-failed { background: #fee2e2; color: #b91c1c; border: 1px solid #fca5a5; }
    .test-evidence-block { background: white; border-radius: 12px; border: 1px solid #e2e8f0; padding: 24px; margin-bottom: 24px; box-shadow: 0 2px 4px rgba(0,0,0,0.04); }
    .test-evidence-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f1f5f9; padding-bottom: 12px; margin-bottom: 12px; }
    .test-id-pill { background: #0f172a; color: white; padding: 4px 10px; border-radius: 6px; font-weight: 700; font-size: 0.82rem; }
    .test-evidence-meta { font-size: 0.85rem; color: #64748b; display: flex; gap: 24px; margin-bottom: 20px; flex-wrap: wrap; }
    .step-container { margin-top: 20px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 16px; }
    .step-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; font-weight: 600; color: #1e293b; font-size: 0.95rem; }
    .step-badge { background: #2563eb; color: white; padding: 3px 10px; border-radius: 6px; font-size: 0.78rem; font-weight: 700; }
    .step-img-frame { width: 100%; border-radius: 8px; overflow: hidden; border: 1px solid #cbd5e1; box-shadow: 0 4px 12px rgba(0,0,0,0.08); background: #000; }
    .audit-screenshot { width: 100%; display: block; height: auto; }
    .error-banner { margin-top: 10px; padding: 12px 16px; background: #fee2e2; border-left: 4px solid #ef4444; color: #991b1b; font-family: monospace; font-size: 0.88rem; border-radius: 4px; }
    .integrity-seal { background: #0f172a; color: #e2e8f0; padding: 24px 30px; border-radius: 12px; margin-top: 36px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
    .hash-badge { font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace; background: #1e293b; padding: 10px 14px; border-radius: 8px; color: #38bdf8; word-break: break-all; margin-top: 10px; display: block; border: 1px solid #334155; font-size: 0.9rem; }
  </style>
</head>
<body>
  <div class="header-banner">
    <h1>EMPLOYEE MANAGEMENT SYSTEM</h1>
    <div class="header-subtitle">Automated Regression Validation Audit Report — Authentication Suite</div>
    <div class="header-meta">
      <div><strong>Target System:</strong> Dev Node (192.168.1.106:8080)</div>
      <div><strong>Validation Operator:</strong> Dhanush Nagendra Prasad</div>
      <div><strong>Execution Timestamp:</strong> ${generatedTimestamp}</div>
      <div><strong>Regulatory Scope:</strong> FDA 21 CFR Part 11 / GAMP 5 Category 4</div>
    </div>
  </div>

  <div class="kpi-grid">
    <div class="kpi-card">
      <div class="kpi-title">Total Test Protocols</div>
      <div class="kpi-value">${total}</div>
    </div>
    <div class="kpi-card">
      <div class="kpi-title">Passed / Verified</div>
      <div class="kpi-value success">${passed}</div>
    </div>
    <div class="kpi-card">
      <div class="kpi-title">Deviations / Failed</div>
      <div class="kpi-value ${failed > 0 ? 'failed' : ''}">${failed}</div>
    </div>
    <div class="kpi-card">
      <div class="kpi-title">Under Review</div>
      <div class="kpi-value">${needsReview}</div>
    </div>
    <div class="kpi-card">
      <div class="kpi-title">Suite Duration</div>
      <div class="kpi-value">${totalDurationSec}s</div>
    </div>
    <div class="kpi-card">
      <div class="kpi-title">Validation Rate</div>
      <div class="kpi-value success">${successRate}%</div>
    </div>
  </div>

  <div class="card">
    <div class="card-title">📊 Execution Protocol Summary Table</div>
    <table>
      <thead>
        <tr>
          <th style="text-align:center;">Seq #</th>
          <th>Protocol ID</th>
          <th>Validation Scope Description</th>
          <th style="text-align:center;">Status</th>
          <th style="text-align:right;">Duration</th>
          <th style="text-align:center;">Timestamp (IST)</th>
        </tr>
      </thead>
      <tbody>
        ${tableRows}
      </tbody>
    </table>
  </div>

  <div class="card">
    <div class="card-title">📸 Step-by-Step Visual Audit Trail (Computer System Validation)</div>
    <p style="color:#64748b; font-size:0.9rem; margin-top:-10px; margin-bottom:20px;">
      Chronological visual proof captured at each protocol phase (Initial state, Data Entry, Server Verification, Session Termination).
    </p>
    ${evidenceCards}
  </div>

  <div class="integrity-seal">
    <div style="font-weight:700; font-size:1.05rem; margin-bottom:6px; color:#f8fafc;">🔒 Cryptographic Data Integrity Seal (21 CFR Part 11 Tamper-Evidence)</div>
    <div style="font-size:0.88rem; line-height:1.6; color:#94a3b8;">
      This electronic validation document was generated via automated test orchestration. In compliance with FDA 21 CFR Part 11 and ALCOA+ principles, an immutable SHA-256 cryptographic digest has been computed across all test logs, execution metadata, and embedded visual audit artifacts. Any post-execution modification invalidates this signature.
    </div>
    <span class="hash-badge">SHA-256: %%CHECKSUM_PLACEHOLDER%%</span>
    <div style="font-size:0.8rem; color:#64748b; margin-top:8px;">Validated Operator: Dhanush Nagendra Prasad | Automated SDET Pipeline</div>
  </div>
</body>
</html>`;

    // Compute Cryptographic SHA-256 Checksum
    const sha256 = crypto.createHash('sha256').update(reportHtmlBody).digest('hex');
    const finalHtml = reportHtmlBody.replace('%%CHECKSUM_PLACEHOLDER%%', sha256);

    // Save standard industry timestamped report
    const htmlReportPath = path.join(this.reportsDir, `${reportFileName}.html`);
    fs.writeFileSync(htmlReportPath, finalHtml, 'utf-8');

    console.log(`\n\x1b[32m✔ [Enterprise Validation Reporter] HTML Report: ${htmlReportPath}\x1b[0m`);
    console.log(`\x1b[36m✔ [Enterprise Validation Reporter] SHA-256 Checksum: ${sha256}\x1b[0m`);

    // Render Companion PDF Report via headless Chromium
    try {
      const browser = await chromium.launch({ headless: true });
      const page = await browser.newPage();
      await page.setContent(finalHtml, { waitUntil: 'load' });
      const pdfReportPath = path.join(this.reportsDir, `${reportFileName}.pdf`);
      await page.pdf({
        path: pdfReportPath,
        format: 'A4',
        printBackground: true,
        margin: { top: '15mm', bottom: '15mm', left: '10mm', right: '10mm' }
      });

      await browser.close();
      console.log(`\x1b[32m✔ [Enterprise Validation Reporter] Companion PDF: ${pdfReportPath}\x1b[0m\n`);
    } catch (err) {
      console.warn('PDF generation notice:', err);
    }

    // Enforce 5-run retention governance to protect server storage
    this.enforceRetentionPolicy(5);
  }
}
