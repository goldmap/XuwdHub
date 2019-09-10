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
    public class JBlogsController : Controller
    {
        private readonly BloggingContext _context;

        public JBlogsController(BloggingContext context)
        {
            _context = context;
        }

        // GET: JBlogs
        public async Task<IActionResult> Index()
        {
            return View(await _context.JBlogs.ToListAsync());
        }

        // GET: JBlogs/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var jBlog = await _context.JBlogs
                .FirstOrDefaultAsync(m => m.JBlogId == id);
            if (jBlog == null)
            {
                return NotFound();
            }

            return View(jBlog);
        }

        // GET: JBlogs/Create
        public IActionResult Create()
        {
            return View();
        }

        // POST: JBlogs/Create
        // To protect from overposting attacks, please enable the specific properties you want to bind to, for 
        // more details see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("JBlogId,Title,Subject,Author,Url")] JBlog jBlog)
        {
            if (ModelState.IsValid)
            {
                _context.Add(jBlog);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            return View(jBlog);
        }

        // GET: JBlogs/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var jBlog = await _context.JBlogs.FindAsync(id);
            if (jBlog == null)
            {
                return NotFound();
            }
            return View(jBlog);
        }

        // POST: JBlogs/Edit/5
        // To protect from overposting attacks, please enable the specific properties you want to bind to, for 
        // more details see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("JBlogId,Title,Subject,Author,Url")] JBlog jBlog)
        {
            if (id != jBlog.JBlogId)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(jBlog);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!JBlogExists(jBlog.JBlogId))
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
            return View(jBlog);
        }

        // GET: JBlogs/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null)
            {
                return NotFound();
            }

            var jBlog = await _context.JBlogs
                .FirstOrDefaultAsync(m => m.JBlogId == id);
            if (jBlog == null)
            {
                return NotFound();
            }

            return View(jBlog);
        }

        // POST: JBlogs/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            var jBlog = await _context.JBlogs.FindAsync(id);
            _context.JBlogs.Remove(jBlog);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool JBlogExists(int id)
        {
            return _context.JBlogs.Any(e => e.JBlogId == id);
        }
    }
}
