using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using WebTest.Models;

namespace WebTest.Controllers
{
    public class JImagesController : Controller
    {
        private readonly BloggingContext _context;

        public JImagesController(BloggingContext context)
        {
            _context = context;
        }
        /*
        // GET: JImages
        public async Task<IActionResult> Index()
        {
            return View(await _context.JImages.ToListAsync());
        }
        */

        public async Task<IActionResult> Index()
        {
            return View(await _context.JImages.ToListAsync());
        }

        // GET: JImages/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var jImage = await _context.JImages
                .FirstOrDefaultAsync(m => m.JImageId == id);
            if (jImage == null)
            {
                return NotFound();
            }

            return View(jImage);
        }

        // GET: JImages/Create
        public IActionResult Create()
        {
            return View();
        }

        // POST: JImages/Create
        // To protect from overposting attacks, please enable the specific properties you want to bind to, for 
        // more details see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("JImageId,Title,Subject,Author,Url")] JImage jImage)
        {
            if (ModelState.IsValid)
            {
                _context.Add(jImage);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            return View(jImage);
        }

        // GET: JImages/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var jImage = await _context.JImages.FindAsync(id);
            if (jImage == null)
            {
                return NotFound();
            }
            return View(jImage);
        }

        // POST: JImages/Edit/5
        // To protect from overposting attacks, please enable the specific properties you want to bind to, for 
        // more details see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("JImageId,Title,Subject,Author,Url")] JImage jImage)
        {
            if (id != jImage.JImageId)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(jImage);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!JImageExists(jImage.JImageId))
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }
            return View(jImage);
        }

        // GET: JImages/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var jImage = await _context.JImages
                .FirstOrDefaultAsync(m => m.JImageId == id);
            if (jImage == null)
            {
                return NotFound();
            }

            return View(jImage);
        }

        // POST: JImages/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var jImage = await _context.JImages.FindAsync(id);
            _context.JImages.Remove(jImage);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool JImageExists(int id)
        {
            return _context.JImages.Any(e => e.JImageId == id);
        }

        public IActionResult GetImage()
        {
            List<JImage> jImages = _context.JImages.ToList();
            var result = new { success = true, data = jImages };
            return Json(result);
        }
    }
}
